import { UIManager } from './UIManager.js';

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
    }

    async checkAuthStatus() {
        try {
            const response = await fetch('/api/auth/is-logged-in');
            this.user = await response.json();
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

        const formData = new FormData(event.target);
        const password = formData.get('password');
        const confirmPassword = formData.get('confirmPassword');

        // Validate passwords match
        if (password !== confirmPassword) {
            this.uiManager.showToast('Passwords do not match', 'error');
            return;
        }

        // Validate password length
        if (password.length < 8) {
            this.uiManager.showToast('Password must be at least 8 characters', 'error');
            return;
        }

        // Validate age
        const age = parseInt(formData.get('age'));
        if (age < 13 || age > 120) {
            this.uiManager.showToast('Please enter a valid age', 'error');
            return;
        }

        const registerData = {
            username: formData.get('username'),
            displayName: formData.get('displayName'),
            email: formData.get('email'),
            password: password,
            age: age,
            address: formData.get('address')
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
                const error = await response.text();
                throw new Error(error || 'Registration failed');
            }

            const user = await response.json();
            this.user = user;
            this.router.navigate('/login');
            this.uiManager.showToast('Registration successful! Welcome!');

        } catch (error) {
            this.uiManager.showToast(error.message, 'error');
        } finally {
            // Restore button
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        }
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
            window.location.href = redirect;
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

        // Show loading state
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

            if (!response.ok) {
                const error = await response.text();
                throw new Error(error || 'Login failed');
            }

            const responseData = await response.json();

            // Check if 2FA is required
            if (responseData.requires2FA) {
                // Store email for verification
                this.pending2FAEmail = data.email;

                // Show 2FA verification page
                this.uiManager.showToast(responseData.message, 'info');
                await this.router.navigate('/2fa/verify');
                return;
            }

            if (!response.ok) {
                throw new Error(responseData.message || 'Login failed');
            }

            // Normal login without 2FA
            this.user = responseData;
            this.isAuthenticated = true;
            localStorage.setItem('user', JSON.stringify(responseData));

            this.uiManager.updateUserDisplay(this.user);
            this.uiManager.showToast('Login successful!');

            // Sync cart and wishlist
            if (this.cartManager) await this.cartManager.syncWithServer();
            if (this.wishlistManager) await this.wishlistManager.syncWithServer();

            // Redirect
            const redirect = sessionStorage.getItem('redirectAfterLogin') || '/';
            sessionStorage.removeItem('redirectAfterLogin');
            window.location.href = redirect;

        } catch (error) {
            this.uiManager.showToast(error.message, 'error');
        } finally {
            loginBtn.innerHTML = originalText;
            loginBtn.disabled = false;
        }
    }

    async handleForgotPassword(event) {
        event.preventDefault();

        const formData = new FormData(event.target);
        const email = formData.get('email');

        const submitBtn = document.getElementById('forgot-password-btn');
        const originalText = submitBtn.innerText;
        submitBtn.innerHTML = '<div class="spinner-small mx-auto"></div>';
        submitBtn.disabled = true;

        try {
            const response = await fetch(`/api/auth/forgot-password?email=${encodeURIComponent(email)}`, {
                method: 'POST'
            });

            // Hide form, show success message
            event.target.classList.add('hidden');
            const successMsg = document.getElementById('forgot-success-message');
            document.getElementById('sent-email').textContent = email;
            successMsg.classList.remove('hidden');

        } catch (error) {
            this.uiManager.showToast('An error occurred. Please try again.', 'error');
        } finally {
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
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
            window.location.href = redirect;

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
            const response = await fetch('/api/2fa/toggle', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ "enabled": enabled })
            });

            if (!response.ok) {
                throw new Error('Failed to update 2FA settings');
            }

            const data = await response.text();
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
            const response = await fetch('/api/2fa/status');
            const data = await response.json();

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
                localStorage.removeItem('user');
                localStorage.removeItem('cartCount');
                localStorage.removeItem('wishlistCount');
                this.user = null;
                this.isAuthenticated = false;
                this.uiManager.updateUserDisplay(null);
                this.uiManager.toggleAuthButtons(false);
                // clear cart on logout and wishlist
                if (this.cartManager) {
                    await this.cartManager.syncWithServer();
                    await this.wishlistManager.syncWithServer();
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

    isAdmin() {
        return this.user?.role === 'ROLE_ADMIN';
    }
}