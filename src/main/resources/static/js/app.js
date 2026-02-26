import { Router } from './core/Router.js';
import { ApiClient } from './core/ApiClient.js';
import { AuthManager } from './modules/AuthManager.js';
import { CartManager } from './modules/CartManager.js';
import { ProductManager } from './modules/ProductManager.js';
import { OrderManager } from './modules/OrderManager.js';
import { AdminManager } from './modules/AdminManager.js';
import { UserManager } from './modules/UserManager.js';
import { UIManager } from './modules/UIManager.js';
import { WishlistManager } from './modules/WishlistManager.js';
import { ComponentStore } from './core/ComponentStore.js';
import { Utils } from './core/Utils.js';
import { TranslationService } from './modules/TranslationService.js';

class App {
    constructor() {
        console.log('App constructor started');

        // Initialize core services (no dependencies)
        this.uiManager = UIManager.getInstance();
        this.router = Router.getInstance();
        this.authManager = AuthManager.getInstance();
        this.componentStore = ComponentStore.getInstance();

        console.log('Core services initialized');

        this.apiClient = new ApiClient(this.authManager, this.router);

        // Initialize managers with ApiClient dependency
        this.cartManager = CartManager.getInstance(this.apiClient);
        this.productManager = ProductManager.getInstance(this.apiClient);
        this.orderManager = OrderManager.getInstance(this.apiClient);
        this.adminManager = AdminManager.getInstance(this.apiClient);
        this.userManager = UserManager.getInstance(this.apiClient);
        this.wishlistManager = WishlistManager.getInstance(this.apiClient);

        console.log('Managers initialized with apiClient');

        // Inject all dependencies into Router
        this.router.setAuthManager(this.authManager);
        this.router.setProductManager(this.productManager);
        this.router.setOrderManager(this.orderManager);
        this.router.setAdminManager(this.adminManager);
        this.router.setCartManager(this.cartManager);
        this.router.setApiClient(this.apiClient);
        this.router.setUserManager(this.userManager);
        this.router.setWishlistManager(this.wishlistManager);
        this.adminManager.setRouter(this.router);
        this.authManager.setWishlistManager(this.wishlistManager);

        // Initialize routes
        this.router.initRoutes();

        // Resolve circular dependencies for AuthManager
        this.authManager.setRouter(this.router);
        this.authManager.setCartManager(this.cartManager);
        this.authManager.setApiClient(this.apiClient);

        console.log('All dependencies resolved');

        // Global mapping for debugging
        window.router = this.router;
        window.cartManager = this.cartManager;
        window.productManager = this.productManager;
        window.authManager = this.authManager;
        window.adminManager = this.adminManager;
        window.orderManager = this.orderManager;
        window.uIManager = this.uiManager;
        window.userManager = this.userManager;
        window.utils = Utils;
        window.wishlistManager = this.wishlistManager;
        window.componentStore = this.componentStore;
        this.translationService = TranslationService.getInstance();
        window.translationService = this.translationService;


        console.log('App constructor completed');
    }

    async init() {
        console.log('App.init started');

        await this.translationService.init();

        // Check authentication status
        await this.authManager.checkAuthStatus();

        console.log('Auth check completed. isAuthenticated:', this.authManager.isAuthenticated);

        // Load user data
        const user = JSON.parse(localStorage.getItem('user'));
        this.uiManager.updateUserDisplay(user);


        // Initialize router
        await this.router.route();
        await this.loadLanguageSwitcher();

        // Setup popstate listener
        window.addEventListener('popstate', () => this.router.route());

        // Mobile menu button
        document.getElementById('mobile-menu-btn')?.addEventListener('click', () => {
            this.uiManager.toggleMobileMenu();
        });

        // User menu dropdown
        document.getElementById('user-menu-button')?.addEventListener('click', (e) => {
            e.stopPropagation();
            this.uiManager.toggleUserDropdown();
        });

        this.setupLanguageSwitcher();

        console.log('App.init completed');
    }

    async loadLanguageSwitcher() {
        const container = document.getElementById('language-switcher-container');
        if (container) {
            const html = await window.componentStore.load('language-switcher');
            container.innerHTML = html;

            // Update button text based on current language
            window.translationService?.updateSwitcherButton();
        }
    }

    setupLanguageSwitcher() {
        // Use event delegation for language switcher
        document.addEventListener('click', async (e) => {
            // Toggle dropdown
            if (e.target.closest('#current-language-btn')) {
                e.stopPropagation();
                const dropdown = document.getElementById('language-dropdown');
                if (dropdown) {
                    dropdown.classList.toggle('hidden');
                }
                return;
            }

            // Handle language selection
            const langOption = e.target.closest('.lang-option');
            if (langOption) {
                const lang = langOption.dataset.lang;
                await this.translationService.translatePage(lang);

                // Hide dropdown
                const dropdown = document.getElementById('language-dropdown');
                if (dropdown) {
                    dropdown.classList.add('hidden');
                }
                return;
            }

            // Close dropdown when clicking outside
            if (!e.target.closest('#language-switcher')) {
                const dropdown = document.getElementById('language-dropdown');
                if (dropdown) {
                    dropdown.classList.add('hidden');
                }
            }
        });
    }
}

// Start the application
const app = new App();
app.init();