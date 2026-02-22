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

    async handleLogin(event) {
        event.preventDefault();
        const formData = new FormData(event.target);
        const data = Object.fromEntries(formData.entries());

        const loginBtn = event.target.querySelector('#login-btn');
        loginBtn.innerHTML = '<div class="m-auto w-6 h-6 spinner"></div>';
        loginBtn.disabled = true;

        let userData;

        try {
            userData = await this.apiClient.fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
        } catch (error) {
            this.uiManager.showToast("Login failed: " + error.message, "error");
            return;
        }

        this.cartManager.syncWithServer(); // Sync cart after login

        this.isAuthenticated = true;
        this.user = {
            ...userData,
            displayName: userData?.displayName || '',
            defaultAddress: userData?.defaultAddress || null
        };
        this.uiManager.updateUserDisplay(this.user);

        await this.router.navigate('/');

        localStorage.setItem('user', JSON.stringify(this.user));

        document.getElementById('userName').innerText = this.user?.email.split('@')[0];
        this.uiManager.showToast('Login successful!');
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