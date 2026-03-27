import { UIManager } from './UIManager.js';
import { User } from '../models/User.js';

export class AuthManager {
    static instance = null;

    constructor() {
        this.user = null;
        this.isAuthenticated = false;
        this.uiManager = UIManager.getInstance();
        this.pending2FAEmail = null;
        this.router = null;
        this.cartManager = null;
        this.wishlistManager = null;
    }

    static getInstance() {
        if (!AuthManager.instance) {
            AuthManager.instance = new AuthManager();
        }
        return AuthManager.instance;
    }

    setRouter(router) {
        this.router = router;
    }

    setCartManager(cartManager) {
        this.cartManager = cartManager;
    }

    setWishlistManager(wishlistManager) {
        this.wishlistManager = wishlistManager;
    }

    setApiClient(apiClient) {
        this.apiClient = apiClient;
    }

    clearStorage() {
        this.isAuthenticated = false;

        localStorage.removeItem('user');
        localStorage.removeItem('cartCount');
        localStorage.removeItem('wishlistCount');

        localStorage.removeItem('cart_sync_completed');
        localStorage.removeItem('wishlist_sync_completed');
    }

    set2FAData(data) {
        this.pending2FAEmail = data.email;
        this.qrCodeUrl = data.qrCodeUrl;
        this.totpSecret = data.totpSecret;
    }

    setResetData(email, totpToken) {
        this.resetEmail = email;
        this.resetTOTPToken = totpToken;
    }

    // Switch between authenticator and email tabs
    switch2FATab(tab) {
        // Update tab styles
        document.querySelectorAll('.tab-2fa').forEach(el => {
            el.classList.remove('border-blue-600', 'text-blue-600');
            el.classList.add('border-transparent', 'text-gray-500');
        });

        if (tab === 'authenticator') {
            document.getElementById('tab-authenticator').classList.add('border-blue-600', 'text-blue-600');
            document.getElementById('tab-authenticator-content').classList.remove('hidden');
            document.getElementById('tab-email-content').classList.add('hidden');

            // Show QR code if available
            this.displayQRCode();
        } else {
            document.getElementById('tab-email').classList.add('border-blue-600', 'text-blue-600');
            document.getElementById('tab-email-content').classList.remove('hidden');
            document.getElementById('tab-authenticator-content').classList.add('hidden');

            // Start email timer
            this.startEmailTimer();
        }
    }

    // Display QR code
    displayQRCode() {
        const loadingEl = document.getElementById('qr-code-loading');
        const qrImage = document.getElementById('qr-code-image');

        if (this.qrCodeUrl) {
            loadingEl.classList.add('hidden');
            qrImage.classList.remove('hidden');
            qrImage.src = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + this.qrCodeUrl;
        }
    }

    // Show manual entry
    showManualCode() {
        document.getElementById('manual-entry').classList.remove('hidden');
        const secretEl = document.getElementById('manual-secret');
        if (this.totpSecret) {
            secretEl.textContent = this.totpSecret;
        }
    }

    // Copy secret to clipboard
    copySecret() {
        const secret = document.getElementById('manual-secret').textContent;
        navigator.clipboard.writeText(secret).then(() => {
            this.uiManager.showToast('Secret copied to clipboard!', 'success');
        });
    }

    async verifyTOTP() {
        const code = document.getElementById('auth-code').value;

        if (!code || code.length !== 6) {
            this.uiManager.showToast('Please enter a valid 6-digit code', 'error');
            return;
        }

        try {
            const response = await fetch('/api/2fa/verify', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    email: this.pending2FAEmail,
                    code: code
                })
            });

            if (!response.ok) {
                throw new Error('Invalid verification code');
            }

            const user = await response.json();
            await this.complete2FALogin(user);

        } catch (error) {
            this.uiManager.showToast(error.message, 'error');
        }
    }

    // Verify email code
    async verifyEmailCode() {
        let code = "";

        Array.from(document.getElementsByClassName("email-code-input")).forEach(el => { code += el.value; });

        if (!code || code.length !== 6) {
            this.uiManager.showToast('Please enter a valid 6-digit code', 'error');
            return;
        }

        try {
            const response = await fetch('/api/2fa/verify', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    email: this.pending2FAEmail,
                    code: code
                })
            });

            if (!response.ok) {
                throw new Error('Invalid verification code');
            }

            const user = await response.json();
            await this.complete2FALogin(user);

        } catch (error) {
            this.uiManager.showToast(error.message, 'error');
        }
    }

    async resendEmailCode() {
        try {
            await fetch(`/api/2fa/resend-email-code?email=${encodeURIComponent(this.pending2FAEmail)}`, {
                method: 'POST'
            });

            this.uiManager.showToast('New code sent to your email!', 'success');
            this.startEmailTimer();

        } catch (error) {
            this.uiManager.showToast('Failed to resend code', 'error');
        }
    }

    // Start email timer
    startEmailTimer() {
        let timeLeft = 300; // 5 minutes in seconds
        const timerEl = document.getElementById('email-timer');

        const interval = setInterval(() => {
            const minutes = Math.floor(timeLeft / 60);
            const seconds = timeLeft % 60;
            timerEl.textContent = `${minutes}:${seconds.toString().padStart(2, '0')}`;

            if (timeLeft <= 0) {
                clearInterval(interval);
                timerEl.textContent = 'Code expired';
            }
            timeLeft--;
        }, 1000);
    }

    // Complete 2FA login
    async complete2FALogin(user) {
        this.user = user;
        this.isAuthenticated = true;
        this.pending2FAEmail = null;
        localStorage.setItem('user', JSON.stringify(user));

        this.uiManager.updateUserDisplay(user);
        this.uiManager.showToast('Verification successful!');

        if (this.cartManager) await this.cartManager.syncWithServer();
        if (this.wishlistManager) await this.wishlistManager.syncWithServer();

        const redirect = sessionStorage.getItem('redirectAfterLogin') || '/';
        sessionStorage.removeItem('redirectAfterLogin');
        await this.router.navigate(redirect);
    }

    async checkAuthStatus() {
        try {
            const response = await fetch('/api/auth/is-logged-in');
            this.user = new User(await response.json());
            this.isAuthenticated = response.ok;

            // Need to be set in localstoarge to be active on refresh
            if (this.pending2FAEmail) {
                return await this.router.navigate("/2fa/verify")
            }

            if (!this.isAuthenticated) {
                this.clearStorage();
                return false;
            }

            localStorage.setItem('user', JSON.stringify(this.user));

            return this.isAuthenticated;
        } catch (error) {
            console.error('Auth check failed:', error);
            this.clearStorage();
            return false;
        }
    }

    async handleRegister(event) {
        event.preventDefault();

        // Clear all previous errors
        this.clearAllErrors();

        const formData = new FormData(event.target);
        const password = formData.get('password');
        const confirmPassword = formData.get('confirmPassword');

        let hasErrors = false;

        // Frontend validation
        // Username validation
        const username = formData.get('username');
        if (!username || username.trim() === '') {
            this.showFieldError('username', 'Username is required');
            hasErrors = true;
        } else if (username.length < 5 || username.length > 20) {
            this.showFieldError('username', 'Username must be between 5 and 20 characters');
            hasErrors = true;
        }

        // Display name validation
        const displayName = formData.get('displayName');
        if (!displayName || displayName.trim() === '') {
            this.showFieldError('displayName', 'Display name is required');
            hasErrors = true;
        }

        // Email validation
        const email = formData.get('email');
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!email || email.trim() === '') {
            this.showFieldError('email', 'Email is required');
            hasErrors = true;
        } else if (!emailRegex.test(email)) {
            this.showFieldError('email', 'Please enter a valid email address');
            hasErrors = true;
        }

        // Age validation
        const age = parseInt(formData.get('age'));
        if (!age || isNaN(age)) {
            this.showFieldError('age', 'Age is required');
            hasErrors = true;
        } else if (age < 13 || age > 120) {
            this.showFieldError('age', 'Please enter a valid age between 13 and 120');
            hasErrors = true;
        }

        // Address validation
        const address = formData.get('address');
        if (!address || address.trim() === '') {
            this.showFieldError('address', 'Address is required');
            hasErrors = true;
        }

        // Password validation
        if (!password || password === '') {
            this.showFieldError('password', 'Password is required');
            hasErrors = true;
        } else if (password.length < 8) {
            this.showFieldError('password', 'Password must be at least 8 characters');
            hasErrors = true;
        }

        // Confirm password validation
        if (!confirmPassword || confirmPassword === '') {
            this.showFieldError('confirmPassword', 'Please confirm your password');
            hasErrors = true;
        } else if (password !== confirmPassword) {
            this.showFieldError('confirmPassword', 'Passwords do not match');
            hasErrors = true;
        }

        // Terms validation
        const terms = formData.get('terms');
        if (!terms) {
            this.showFieldError('terms', 'You must agree to the terms and conditions');
            hasErrors = true;
        }

        if (hasErrors) {
            return;
        }

        const registerData = {
            username: username.trim(),
            displayName: displayName.trim(),
            email: email.trim(),
            password: password,
            age: age,
            address: address.trim()
        };

        // Show loading state
        const submitBtn = event.target.querySelector('button[type="submit"]');
        const originalText = submitBtn.innerText;
        submitBtn.innerHTML = '<div class="spinner-small mx-auto"></div>';
        submitBtn.disabled = true;

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(registerData)
            });

            if (!response.ok) {
                const errorData = await response.json();

                // Handle field-specific errors from backend
                if (errorData && typeof errorData === 'object') {
                    let hasBackendErrors = false;

                    // Loop through each field error
                    Object.entries(errorData).forEach(([field, message]) => {
                        // Map backend field names to form field IDs if needed
                        const fieldMap = {
                            'username': 'username',
                            'displayName': 'displayName',
                            'email': 'email',
                            'age': 'age',
                            'address': 'address',
                            'password': 'password'
                        };

                        const formField = fieldMap[field];
                        if (formField) {
                            this.showFieldError(formField, message);
                            hasBackendErrors = true;
                        }
                    });

                    if (hasBackendErrors) {
                        return;
                    }
                }

                // If no field-specific errors, show general error
                throw new Error(errorData.message || 'Registration failed');
            }

            const user = await response.json();
            this.user = user;
            this.uiManager.showToast('Registration successful! Please login.', 'success');
            setTimeout(() => {
                this.router.navigate('/login');
            }, 1500);

        } catch (error) {
            console.error('Registration error:', error);
            this.uiManager.showToast(error.message || 'Registration failed', 'error');
        } finally {
            // Restore button
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        }
    }

    showFieldError(fieldId, message) {
        const field = document.getElementById(fieldId);
        const errorDiv = document.getElementById(`${fieldId}-error`);
        const formGroup = field?.closest('.form-group');

        if (field && errorDiv) {
            // Add error class to form group
            if (formGroup) {
                formGroup.classList.add('error');
            }

            // Add error class to field
            field.classList.add('border-red-500', 'bg-red-50');

            // Show error message
            errorDiv.textContent = message;
            errorDiv.classList.remove('hidden');

            // Scroll to first error
            if (!document.querySelector('.form-group.error:not(.scrolled-to)')) {
                field.scrollIntoView({ behavior: 'smooth', block: 'center' });
                formGroup?.classList.add('scrolled-to');
            }
        }
    }

    clearAllErrors() {
        // Remove all error classes and hide error messages
        document.querySelectorAll('.form-group.error').forEach(group => {
            group.classList.remove('error', 'scrolled-to');
        });

        document.querySelectorAll('.border-red-500.bg-red-50').forEach(field => {
            field.classList.remove('border-red-500', 'bg-red-50');
        });

        document.querySelectorAll('[id$="-error"]').forEach(errorDiv => {
            errorDiv.classList.add('hidden');
            errorDiv.textContent = '';
        });
    }

    async autoLogin() {
        // Auto login after registration, will apply email verfication in the future
        const loginResponse = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: registerData.email,
                password: registerData.password
            })
        });

        if (loginResponse.ok) {
            const loggedInUser = await loginResponse.json();
            this.user = loggedInUser;
            this.isAuthenticated = true;
            localStorage.setItem('user', JSON.stringify(loggedInUser));

            this.uiManager.updateUserDisplay(loggedInUser);
            this.uiManager.showToast('Registration successful! Welcome!');

            // Sync cart and wishlist
            if (this.cartManager) await this.cartManager.syncWithServer();
            if (this.wishlistManager) await this.wishlistManager.syncWithServer();

            // Redirect to home or previous page
            const redirect = sessionStorage.getItem('redirectAfterLogin') || '/';
            sessionStorage.removeItem('redirectAfterLogin');
            await this.router.navigate(redirect);
        }
    }

    // Google registration
    googleRegister() {
        // Store the current page to redirect back after OAuth
        sessionStorage.setItem('redirectAfterLogin', window.location.pathname);

        // Redirect to Google OAuth endpoint
        window.location.href = '/oauth2/authorization/google';
    }

    async googleLogin() {
        // Store the current page to redirect back after OAuth
        sessionStorage.setItem('redirectAfterLogin', window.location.pathname);

        // Show loading toast
        this.uiManager.showToast('Redirecting to Google...', 'info');

        // Redirect to Google OAuth endpoint
        window.location.href = '/oauth2/authorization/google';
    }

    async handleLogin(event) {
        event.preventDefault();

        const formData = new FormData(event.target);
        const data = Object.fromEntries(formData.entries());

        const loginBtn = event.target.querySelector('#login-btn');
        const originalText = loginBtn.innerText;
        loginBtn.innerHTML = '<div class="spinner-small mx-auto"></div>';
        loginBtn.disabled = true;

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            const responseData = await response.json();

            if (responseData.requires2FA) {
                this.set2FAData({
                    email: data.email,
                    qrCodeUrl: responseData.qrCodeUrl,
                    totpSecret: responseData.totpSecret
                });

                this.uiManager.showToast(responseData.message, 'info');
                await this.router.navigate('/2fa/verify');
                return;
            }

            if (!response.ok) {
                throw new Error(responseData.message || 'Login failed');
            }

            this.user = new User(responseData);

            this.isAuthenticated = true;
            localStorage.setItem('user', JSON.stringify(responseData));

            this.uiManager.updateUserDisplay(this.user);
            this.uiManager.showToast('Login successful!');

            // Sync cart from localStorage to server
            if (this.cartManager) {
                await this.cartManager.syncWithServer();
            }

            if (this.wishlistManager) {
                await this.wishlistManager.syncWithServer();
            }

            const redirect = sessionStorage.getItem('redirectAfterLogin') || '/';
            sessionStorage.removeItem('redirectAfterLogin');
            window.router.navigate(redirect || '/profile');

        } catch (error) {
            this.uiManager.showToast(error.message, 'error');
        } finally {
            loginBtn.innerHTML = originalText;
            loginBtn.disabled = false;
        }
    }

    // Switch between reset tabs
    switchResetTab(tab) {
        document.querySelectorAll('.tab-reset').forEach(el => {
            el.classList.remove('border-blue-600', 'text-blue-600');
            el.classList.add('border-transparent', 'text-gray-500');
        });

        if (tab === 'email') {
            document.getElementById('tab-email-reset').classList.add('border-blue-600', 'text-blue-600');
            document.getElementById('tab-email-reset-content').classList.remove('hidden');
            document.getElementById('tab-authenticator-reset-content').classList.add('hidden');
        } else {
            document.getElementById('tab-authenticator-reset').classList.add('border-blue-600', 'text-blue-600');
            document.getElementById('tab-authenticator-reset-content').classList.remove('hidden');
            document.getElementById('tab-email-reset-content').classList.add('hidden');
        }
    }

    async handleResetPassword(event) {
        event.preventDefault();

        const formData = new FormData(event.target);
        const token = formData.get('token');
        const newPassword = formData.get('newPassword');
        const confirmPassword = document.getElementById('confirm-password').value;

        // Validate passwords match
        if (newPassword !== confirmPassword) {
            this.uiManager.showToast('Passwords do not match', 'error');
            return;
        }

        const submitBtn = document.getElementById('reset-password-btn');
        const originalText = submitBtn.innerText;
        submitBtn.innerHTML = '<div class="spinner-small mx-auto"></div>';
        submitBtn.disabled = true;

        try {
            const response = await fetch('/api/auth/reset-password', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ token, newPassword })
            });

            if (!response.ok) {
                const error = await response.text();
                throw new Error(error);
            }

            // Hide form, show success message
            document.getElementById('reset-password-form').classList.add('hidden');
            document.getElementById('reset-success-message').classList.remove('hidden');

        } catch (error) {
            // Show error message
            document.getElementById('reset-password-form').classList.add('hidden');
            document.getElementById('reset-error-message').classList.remove('hidden');
            document.getElementById('error-message-text').textContent = error.message;
        } finally {
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        }
    }

    // Handle forgot password (email)
    async handleForgotPassword(event) {
        event.preventDefault();

        const formData = new FormData(event.target);
        const email = formData.get('email');

        const submitBtn = document.getElementById('forgot-password-btn');
        const originalText = submitBtn?.innerText;
        submitBtn.innerHTML = '<div class="spinner-small mx-auto"></div>';
        submitBtn.disabled = true;

        try {
            const response = await fetch(`/api/auth/forgot-password?email=${encodeURIComponent(email)}`, {
                method: 'POST'
            });

            // Hide form, show success message
            document.getElementById('forgot-password-form').classList.add('hidden');
            document.getElementById('tab-email-reset-content').classList.add('hidden');

            const successMsg = document.getElementById('reset-success-message');
            if (document.getElementById('sent-email')) {
                document.getElementById('sent-email').textContent = email;
            }
            document.getElementById('success-title').textContent = 'Check Your Email';
            document.getElementById('success-message').innerHTML =
                `If an account exists for <span class="font-semibold">${email}</span>, you'll receive a password reset link shortly.`;
            document.getElementById('success-footer').innerHTML =
                `Didn't receive it? Check your spam folder or <button onclick="window.authManager.resetForm()" class="text-blue-600 hover:text-blue-800 font-medium">try again</button>`;
            successMsg.classList.remove('hidden');

        } catch (error) {
            console.error(error.message);
            this.uiManager.showToast('An error occurred. Please try again.' + error.message, 'error');
        } finally {
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        }
    }

    // Reset form (try again)
    resetForm() {
        document.getElementById('reset-success-message').classList.add('hidden');
        document.getElementById('tab-email-reset-content').classList.remove('hidden');
        document.getElementById('forgot-password-form').classList.remove('hidden');
        document.getElementById('new-password-form').classList.add('hidden');

        // Clear inputs
        document.getElementById('email').value = '';
        document.getElementById('auth-email').value = '';
        document.getElementById('auth-reset-code').value = '';
        document.getElementById('new-password-totp').value = '';
        document.getElementById('confirm-password-totp').value = '';

        this.switchResetTab('email');
    }

    // Remember: Not used
    async handle2FAVerification(event) {
        event.preventDefault();

        const formData = new FormData(event.target);
        const code = formData.get('code');
        const email = formData.get('email') || this.pending2FAEmail;

        if (!code || code.length !== 6) {
            this.uiManager.showToast('Please enter a valid 6-digit code', 'error');
            return;
        }

        const verifyBtn = document.getElementById('verify-btn');
        const originalText = verifyBtn.innerText;
        verifyBtn.innerHTML = '<div class="spinner-small mx-auto"></div>';
        verifyBtn.disabled = true;

        try {
            const response = await fetch('/api/2fa/verify', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, code })
            });

            if (!response.ok) {
                const error = await response.text();
                throw new Error(error || 'Invalid verification code');
            }

            const user = await response.json();

            // Login successful
            this.user = user;
            this.isAuthenticated = true;
            this.pending2FAEmail = null;
            localStorage.setItem('user', JSON.stringify(user));

            this.uiManager.updateUserDisplay(user);
            this.uiManager.showToast('2FA verification successful!');

            // Sync cart and wishlist
            if (this.cartManager) await this.cartManager.syncWithServer();
            if (this.wishlistManager) await this.wishlistManager.syncWithServer();

            // Redirect
            const redirect = sessionStorage.getItem('redirectAfterLogin') || '/';
            sessionStorage.removeItem('redirectAfterLogin');
            await this.router.navigate(redirect);

        } catch (error) {
            this.uiManager.showToast(error.message, 'error');
            // Clear code inputs
            document.querySelectorAll('.code-input').forEach(input => input.value = '');
            document.getElementById('2fa-code').value = '';
            document.querySelector('.code-input')?.focus();
        } finally {
            verifyBtn.innerHTML = originalText;
            verifyBtn.disabled = false;
        }
    }

    async resend2FACode() {
        if (!this.pending2FAEmail) {
            this.uiManager.showToast('Session expired. Please login again.', 'error');
            await this.router.navigate('/login');
            return;
        }

        const resendBtn = event.target;
        const originalText = resendBtn.innerText;
        resendBtn.innerHTML = 'Sending...';
        resendBtn.disabled = true;

        try {
            const response = await fetch('/api/2fa/resend', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: this.pending2FAEmail })
            });

            if (!response.ok) {
                throw new Error('Failed to resend code');
            }

            this.uiManager.showToast('New code sent to your email!', 'success');

        } catch (error) {
            this.uiManager.showToast(error.message, 'error');
        } finally {
            resendBtn.innerHTML = originalText;
            resendBtn.disabled = false;
        }
    }

    async toggle2FA() {
        const checkbox = document.getElementById('2fa-toggle');
        const enabled = checkbox.checked;

        try {
            const data = await this.apiClient.fetch('/api/2fa/toggle', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ "enabled": enabled })
            });

            this.uiManager.showToast(data.message || `2FA ${enabled ? 'enabled' : 'disabled'} successfully!`);

            // Update status display
            const statusEl = document.getElementById('2fa-status');
            if (statusEl) {
                statusEl.innerHTML = enabled
                    ? '<span class="text-green-600">✓ Two-factor authentication is enabled</span>'
                    : '<span class="text-gray-500">Two-factor authentication is disabled</span>';
            }

        } catch (error) {
            this.uiManager.showToast(error.message, 'error');
            // Revert checkbox
            checkbox.checked = !enabled;
        }
    }

    async check2FAStatus() {
        try {
            const data = await this.apiClient.fetch('/api/2fa/status');

            const checkbox = document.getElementById('2fa-toggle');
            const statusEl = document.getElementById('2fa-status');

            if (checkbox) checkbox.checked = data.is2faEnabled;
            if (statusEl) {
                statusEl.innerHTML = data.is2faEnabled
                    ? '<span class="text-green-600">✓ Two-factor authentication is enabled</span>'
                    : '<span class="text-gray-500">Two-factor authentication is disabled</span>';
            }

        } catch (error) {
            console.error('Error checking 2FA status:', error);
        }
    }

    async logout() {
        try {
            const response = await fetch('/api/auth/logout', { method: 'POST' });
            if (response.ok) {
                this.user = null;
                this.isAuthenticated = false;
                this.uiManager.updateUserDisplay(null);
                this.uiManager.toggleAuthButtons(false);

                // this will clear all stored data
                localStorage.clear();

                try {
                    await this.cartManager.syncWithServer();
                    await this.wishlistManager.syncWithServer();
                } catch (error) {

                }

                if (this.router) {
                    await this.router.navigate('/login');
                }
                return true;
            }
        } catch (error) {
            this.uiManager.showToast('Logout failed', 'error');
            return false;
        }
    }

    clearStoredLocalStorage() {
        localStorage.removeItem('user');
        localStorage.removeItem('cartCount');
        localStorage.removeItem('wishlistCount');
        localStorage.removeItem('cart_sync_completed');
        localStorage.removeItem('wishlist_sync_completed');
        localStorage.removeItem('guest_cart');

        this.wishlistManager.clearSyncFlag();

        this.cartManager.items = [];
        this.cartManager.clearSyncFlag();
        this.cartManager.clearLocalStorage();
        this.cartManager.render();
        this.cartManager.updateBadge();
    }

    isAdmin() {
        return this.user?.role === 'ROLE_ADMIN';
    }
}