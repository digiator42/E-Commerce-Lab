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
            this.uiManager.showToast('Failed to update profile: ' + error, 'error');
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

    async showTab(tabName) {
        // Update tab buttons
        document.querySelectorAll('.profile-tab').forEach(btn => {
            btn.classList.remove('active', 'bg-blue-50', 'text-blue-600');
            btn.classList.add('text-gray-500', 'hover:bg-gray-50');
        });

        const activeBtn = document.querySelector(`[data-tab="${tabName}"]`);
        if (activeBtn) {
            activeBtn.classList.add('active', 'bg-blue-50', 'text-blue-600');
            activeBtn.classList.remove('text-gray-500', 'hover:bg-gray-50');
        }

        // Show selected tab content
        document.querySelectorAll('.profile-tab-content').forEach(tab => {
            tab.classList.add('hidden');
        });

        const selectedTab = document.getElementById(`tab-${tabName}`);
        if (selectedTab) {
            selectedTab.classList.remove('hidden');

            // If it's the redeem tab, refresh the balance
            if (tabName === 'redeem') {
                // await this.loadStoreBalance();
                await this.loadRedeemHistory();
            }
        }
    }

    async loadRedeemHistory() {
        try {
            const response = await this.apiClient.fetch('/api/gift-cards/history');

            const history = response.history;
            const balance = response.userStoreBalance || 0;

            const balanceDisplay = document.getElementById('store-balance-display');

            if (balanceDisplay) {
                balanceDisplay.textContent = `$${balance.toFixed(2)}`;
            }
            this.renderRedeemHistory(history);
        } catch (error) {
            console.error('Failed to load redeem history:', error);
            // Show placeholder fallback
            this.renderRedeemHistory([]);
        }
    }

    // Render redeem history
    renderRedeemHistory(history) {
        const historyContainer = document.getElementById('redeem-history');
        if (!historyContainer) return;

        if (!history || history.length === 0) {
            historyContainer.innerHTML = `
            <div class="text-center text-gray-400 py-4">
                No recent activity
            </div>
        `;
            return;
        }

        historyContainer.innerHTML = history.map(item => ` 
        <div class="flex items-center justify-between p-3 bg-gray-50 rounded-xl ${item.type === 'REDEEM' ? 'bg-green-100' : 'bg-red-100'}">
            <div class="flex items-center">
                <div class="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center mr-3">
                    <svg class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                    </svg>
                </div>
                <div>
                    <p class="font-medium text-gray-900">$${item.amount.toFixed(2)}</p>
                    <p class="text-xs text-gray-500">${new Date(item.date).toLocaleDateString()}</p>
                </div>
            </div>
            <span class="text-xs text-gray-400">${item.code}</span>
            <span class="text-xs text-gray-400">${item.type}</span>
        </div>
    `).join('');
    }

    // Handle redeem form submit
    async handleRedeemSubmit(event) {
        event.preventDefault();

        const codeInput = document.getElementById('giftcard-code');
        const redeemBtn = document.getElementById('redeem-btn');
        const code = codeInput.value.trim().toUpperCase();

        if (!code) {
            this.uiManager.showToast('Please enter a gift card code', 'error', 3000);
            return;
        }

        // Simple validation for gift card format
        // if (!code.startsWith('GIFT-')) {
        //     this.uiManager.showToast('Invalid gift card format. Code should start with GIFT-', 'error', 4000);
        //     return;
        // }

        // Disable button while processing
        redeemBtn.disabled = true;
        redeemBtn.innerHTML = '<span class="opacity-0">Redeeming...</span><div class="absolute inset-0 flex items-center justify-center"><div class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div></div>';
        redeemBtn.classList.add('relative', 'opacity-80');

        try {
            const data = await this.apiClient.fetch(`/api/gift-cards/redeem?code=${encodeURIComponent(code)}`, {
                method: 'POST'
            });

            // Show success message
            this.uiManager.showToast('Gift card redeemed successfully!', 'success', 4000);

            // Clear input
            codeInput.value = '';

            // Update balance display
            const balanceDisplay = document.getElementById('store-balance-display');
            if (balanceDisplay) {
                balanceDisplay.textContent = `$${data.newBalance.toFixed(2)}`;
            }

            // Add to history (temporary until we refresh)
            this.addToRedeemHistory(code, data.amount || 0);

            // Refresh history if endpoint exists
            await this.loadRedeemHistory();


        } catch (error) {
            this.uiManager.showToast(error, 'error', 4000);
        } finally {
            // Re-enable button
            redeemBtn.disabled = false;
            redeemBtn.innerHTML = 'Redeem';
            redeemBtn.classList.remove('relative', 'opacity-80');
        }
    }

    // Temporary method to add to history (remove when backend endpoint is ready)
    addToRedeemHistory(code, amount) {
        const historyContainer = document.getElementById('redeem-history');
        if (!historyContainer) return;

        // Remove "No recent activity" if present
        if (historyContainer.children.length === 1 && historyContainer.children[0].textContent.includes('No recent activity')) {
            historyContainer.innerHTML = '';
        }

        const today = new Date();
        const historyItem = document.createElement('div');
        historyItem.className = 'flex items-center justify-between p-3 bg-gray-50 rounded-xl animate-fade-in';
        historyItem.innerHTML = `
        <div class="flex items-center">
            <div class="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center mr-3">
                <svg class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                </svg>
            </div>
            <div>
                <p class="font-medium text-gray-900">$${amount.toFixed(2)} added</p>
                <p class="text-xs text-gray-500">${today.toLocaleDateString()}</p>
            </div>
        </div>
        <span class="text-xs text-gray-400">${code}</span>
    `;

        historyContainer.insertBefore(historyItem, historyContainer.firstChild);

        // Limit to 5 items
        while (historyContainer.children.length > 5) {
            historyContainer.removeChild(historyContainer.lastChild);
        }
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