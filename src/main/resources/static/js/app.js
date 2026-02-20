import { Router } from './core/Router.js';
import { ApiClient } from './core/ApiClient.js';
import { AuthManager } from './modules/AuthManager.js';
import { CartManager } from './modules/CartManager.js';
import { ProductManager } from './modules/ProductManager.js';
import { OrderManager } from './modules/OrderManager.js';
import { AdminManager } from './modules/AdminManager.js';
import { UIManager } from './modules/UIManager.js';
import { ComponentStore } from './core/ComponentStore.js';
import { Utils } from './core/Utils.js';

class App {
    constructor() {
        console.log('App constructor started');
        
        // Initialize core services (no dependencies)
        this.router = Router.getInstance();
        this.authManager = AuthManager.getInstance();
        this.uiManager = UIManager.getInstance();
        this.componentStore = ComponentStore.getInstance();

        console.log('Core services initialized');

        // ApiClient (depends on authManager and router)
        this.apiClient = new ApiClient(this.authManager, this.router);

        // Initialize managers with ApiClient dependency
        this.cartManager = CartManager.getInstance(this.apiClient);
        this.productManager = ProductManager.getInstance(this.apiClient);
        this.orderManager = OrderManager.getInstance(this.apiClient);
        this.adminManager = AdminManager.getInstance(this.apiClient);

        console.log('Managers initialized with apiClient');

        // Inject all dependencies into Router
        this.router.setAuthManager(this.authManager);
        this.router.setProductManager(this.productManager);
        this.router.setOrderManager(this.orderManager);
        this.router.setAdminManager(this.adminManager);
        this.router.setCartManager(this.cartManager);
        this.adminManager.setRouter(this.router);
        this.router.setApiClient(this.apiClient);
        
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
        window.utils = Utils;

        console.log('App constructor completed');
    }

    async init() {
        console.log('App.init started');
        
        // Check authentication status
        await this.authManager.checkAuthStatus();
        console.log('Auth check completed. isAuthenticated:', this.authManager.isAuthenticated);
        
        // Load user data
        const user = JSON.parse(localStorage.getItem('user'));
        this.uiManager.updateUserDisplay(user);
        
        // Sync cart if authenticated
        if (this.authManager.isAuthenticated) {
            console.log('User is authenticated, syncing cart...');
            await this.cartManager.syncWithServer();
        }
        
        // Initialize router
        await this.router.route();
        
        // Setup popstate listener
        window.addEventListener('popstate', () => this.router.route());
        
        console.log('App.init completed');
    }
}

// Start the application
const app = new App();
app.init();