import { ApiClient } from '../core/ApiClient.js';
import { UIManager } from './UIManager.js';
import { AuthManager } from './AuthManager.js';
import { User } from '../models/User.js';

export class UserManager {
    static instance = null;

    constructor(apiClient) {
        this.apiClient = apiClient;
        this.uiManager = UIManager.getInstance();
        this.authManager = AuthManager.getInstance();
        this.currentUser = null;
    }

    static getInstance(apiClient) {
        if (!UserManager.instance) {
            UserManager.instance = new UserManager(apiClient);
        }
        return UserManager.instance;
    }

    async loadUserProfile() {
        try {
            this.currentUser = new User(this.authManager.user);
            return this.currentUser;
        } catch (error) {
            console.error('Error loading user profile:', error);
            return null;
        }
    }

    async updateProfile(profileData) {
        try {
            const response = await this.apiClient.fetch('/api/users/profile', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(profileData)
            });

            // Update local user data
            if (this.authManager.user) {
                this.authManager.user.displayName = profileData.displayName;
                this.authManager.user.defaultAddress = profileData.defaultAddress;
                localStorage.setItem('user', JSON.stringify(this.authManager.user));

                this.uiManager.updateUserDisplay(this.authManager.user);
            }

            this.uiManager.showToast('Profile updated successfully!');
            return true;
        } catch (error) {
            this.uiManager.showToast('Failed to update profile: ' + error.message, 'error');
            return false;
        }
    }

    async changePassword(currentPassword, newPassword, confirmPassword) {
        if (newPassword !== confirmPassword) {
            this.uiManager.showToast('New passwords do not match', 'error');
            return false;
        }

        try {
            await this.apiClient.fetch('/api/users/profile', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    currentPassword,
                    newPassword,
                    confirmPassword
                })
            });

            this.uiManager.showToast('Password changed successfully!');
            return true;
        } catch (error) {
            this.uiManager.showToast('Failed to change password: ' + error.message, 'error');
            return false;
        }
    }

    showTab(tabName) {
        // Update tab buttons
        document.querySelectorAll('.profile-tab').forEach(tab => {
            const isActive = tab.dataset.tab === tabName;
            if (isActive) {
                tab.classList.add('bg-blue-50', 'text-blue-600');
                tab.classList.remove('text-gray-500', 'hover:bg-gray-50');
            } else {
                tab.classList.remove('bg-blue-50', 'text-blue-600');
                tab.classList.add('text-gray-500', 'hover:bg-gray-50');
            }
        });

        // Show selected content
        document.querySelectorAll('.profile-tab-content').forEach(content => {
            content.classList.add('hidden');
        });
        document.getElementById(`tab-${tabName}`).classList.remove('hidden');
    }

    async handleInfoSubmit(event) {
        event.preventDefault();

        const displayName = document.getElementById('display-name').value;

        const success = await this.updateProfile({
            displayName: displayName
        });

        if (success) {
            // Update UI
            document.getElementById('profile-display-name').textContent = displayName || this.authManager.user?.email?.split('@')[0];

            const initials = document.getElementById('profile-initials');
            if (initials) {
                initials.textContent = (displayName || this.authManager.user?.email || 'U').charAt(0).toUpperCase();
            }
        }
    }

    async handleAddressSubmit(event) {
        event.preventDefault();

        const address = {
            street: document.getElementById('address-street').value,
            city: document.getElementById('address-city').value,
            state: document.getElementById('address-state').value,
            zipCode: document.getElementById('address-zip').value,
            country: document.getElementById('address-country').value
        };

        // Validate address
        if (!address.street || !address.city || !address.state || !address.zipCode) {
            this.uiManager.showToast('Please fill in all address fields', 'error');
            return;
        }

        await this.updateProfile({
            defaultAddress: JSON.stringify(address)
        });
    }

    async handlePasswordSubmit(event) {
        event.preventDefault();

        const currentPassword = document.getElementById('current-password').value;
        const newPassword = document.getElementById('new-password').value;
        const confirmPassword = document.getElementById('confirm-password').value;

        await this.changePassword(currentPassword, newPassword, confirmPassword);

        // Clear password fields
        document.getElementById('current-password').value = '';
        document.getElementById('new-password').value = '';
        document.getElementById('confirm-password').value = '';
    }
}