import { UIManager } from './UIManager.js';

export class AuthManager {
    static instance = null;

    constructor() {
        this.user = null;
        this.isAuthenticated = false;
        this.uiManager = UIManager.getInstance();

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

    async checkAuthStatus() {
        try {
            const response = await fetch('/api/auth/is-logged-in');
            this.user = await response.json();
            this.isAuthenticated = response.ok;
            if (this.isAuthenticated) {
                localStorage.setItem('user', JSON.stringify(this.user));
            }
            return this.isAuthenticated;
        } catch (error) {
            console.error('Auth check failed:', error);
            this.isAuthenticated = false;
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

            const user = await response.json();

            this.user = user;
            this.isAuthenticated = true;
            localStorage.setItem('user', JSON.stringify(user));

            this.uiManager.updateUserDisplay(user);
            this.uiManager.showToast('Login successful!');

            // Sync cart and wishlist
            if (this.cartManager) await this.cartManager.syncWithServer();
            if (this.wishlistManager) await this.wishlistManager.syncWithServer();

            this.router.navigate('/');

            // Redirect to home or previous page
            // const redirect = sessionStorage.getItem('redirectAfterLogin') || '/';
            // sessionStorage.removeItem('redirectAfterLogin');
            // window.location.href = redirect;

        } catch (error) {
            this.uiManager.showToast(error.message, 'error');
        } finally {
            // Restore button
            loginBtn.innerHTML = originalText;
            loginBtn.disabled = false;
        }
    }

    async logout() {
        try {
            const response = await fetch('/api/auth/logout', { method: 'POST' });
            if (response.ok) {
                localStorage.removeItem('user');
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