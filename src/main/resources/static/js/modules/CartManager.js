import { UIManager } from './UIManager.js';
import { AuthManager } from './AuthManager.js';
import { WishlistManager } from './WishlistManager.js';
import { Product } from '../models/Product.js';

export class CartManager {
    static instance = null;

    constructor(apiClient) {
        this.items = [];
        this.isOpen = false;
        this.apiClient = apiClient;
        this.authManager = AuthManager.getInstance();
        this.uiManager = UIManager.getInstance();
        this.wishlistManager = WishlistManager.getInstance(apiClient);
        this.storageKey = 'guest_cart';
        this.syncCompletedKey = 'cart_sync_completed';
    }

    static getInstance(apiClient) {
        if (!CartManager.instance) {
            CartManager.instance = new CartManager(apiClient);
        }
        return CartManager.instance;
    }

    // Check if user has local cart items
    hasLocalCartItems() {
        try {
            const savedCart = localStorage.getItem(this.storageKey);
            return savedCart && JSON.parse(savedCart).length > 0;
        } catch {
            return false;
        }
    }

    // Check if sync was already completed for this user
    wasSyncCompleted() {
        const userId = this.authManager.user?.id;
        if (!userId) return false;

        const syncKey = `${this.syncCompletedKey}`;
        return localStorage.getItem(syncKey) === 'true';
    }

    // Mark sync as completed for current user
    markSyncCompleted() {
        const userId = this.authManager.user?.id;
        if (userId) {
            const syncKey = `${this.syncCompletedKey}`;
            localStorage.setItem(syncKey, 'true');
        }
    }

    // Clear sync flag (when user logs out)
    clearSyncFlag() {
        const userId = this.authManager.user?.id;
        if (userId) {
            const syncKey = `${this.syncCompletedKey}`;
            localStorage.removeItem(syncKey);
        }
    }

    // Load cart from localStorage for guest users
    loadFromLocalStorage() {
        try {
            const savedCart = localStorage.getItem(this.storageKey);
            console.log("=====> saved ", savedCart);
            if (savedCart) {
                const rawItems = JSON.parse(savedCart);
                // Convert each raw item to a Product instance
                this.items = rawItems.map(item => {
                    if (item.isGiftCard) {
                        return new Product({
                            ...item,
                            stock: -1,
                            category: 'Gift Card'
                        });
                    } else {
                        return Product.fromLocalStorage(item);
                    }
                });
                console.log('Loaded cart from localStorage:', this.items.map(i => i.name));
            } else {
                this.items = [];
            }
            this.render();
            this.updateBadge();
        } catch (error) {
            console.error('Error loading cart from localStorage:', error);
            this.items = [];
        }
    }

    // Save cart to localStorage for guest users
    saveToLocalStorage() {
        try {
            // Convert Product instances to plain objects for storage
            const itemsToStore = this.items.map(item => {
                if (item instanceof Product) {
                    return {
                        id: item.cartItemId,
                        productId: item.id,
                        name: item.name,
                        price: item.price,
                        quantity: item.quantity,
                        imageUrl: item.imageUrl,
                        isGiftCard: item.isGiftCard,
                        recipientEmail: item.recipientEmail,
                        message: item.message,
                        category: item.category,
                        stock: item.stock
                    };
                }
                return item;
            });

            localStorage.setItem(this.storageKey, JSON.stringify(itemsToStore));
            console.log('Saved cart to localStorage:', itemsToStore);
        } catch (error) {
            console.error('Error saving cart to localStorage:', error);
        }
    }

    // Clear localStorage cart
    clearLocalStorage() {
        localStorage.removeItem(this.storageKey);
        console.log('Cleared localStorage cart');
    }

    // Sync local cart with server after login
    async syncLocalCartWithServer() {
        // Don't sync if not authenticated
        if (!this.authManager.isAuthenticated) return;

        // Load current local items FIRST to check
        this.loadFromLocalStorage();

        // Check if we have local items to sync
        const hasLocalItems = this.items.length > 0;

        // Check if sync was already completed for this user
        const syncCompleted = this.wasSyncCompleted();

        console.log('Sync check:', {
            hasLocalItems,
            syncCompleted,
            authenticated: this.authManager.isAuthenticated,
            items: this.items.map(i => i.name)
        });

        // If sync already completed for this user, just load from server
        if (syncCompleted) {
            console.log('Sync already completed for this user, loading from server');
            await this.loadFromServer();
            return;
        }

        // If no local items, just load from server and mark sync as completed
        if (!hasLocalItems) {
            console.log('No local items, loading from server');
            await this.loadFromServer();
            this.markSyncCompleted();
            return;
        }

        // We have local items and sync not completed - perform sync
        console.log('Syncing local cart with server:', this.items);

        try {
            // Add each item from local cart to server
            for (const item of this.items) {
                let i = -1;
                if (item.isGiftCard) {
                    await this.apiClient.fetch('/api/cart/add-gift-card', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            amount: item.price,
                            recipientEmail: this.authManager.user.email,
                            message: item.message || ""
                        })
                    });
                } else {
                    while(++i < item.quantity) {
                        await this.apiClient.fetch(`/api/cart/add/${item.id}`, {
                            method: 'POST'
                        });
                    }
                }
            }

            // Clear local cart AFTER successful sync
            this.clearLocalStorage();
            this.items = []; // Clear in-memory items

            // Mark sync as completed for this user
            this.markSyncCompleted();

            // Load updated cart from server
            await this.loadFromServer();

            this.uiManager.showToast('Your cart has been synced with your account!', 'success');

        } catch (error) {
            console.error('Error syncing local cart with server:', error);
            this.uiManager.showToast('Error syncing your cart. Please try again.', 'error');
        }
    }

    async loadFromServer() {
        if (!this.authManager.isAuthenticated) return;

        try {
            const serverItems = await this.apiClient.fetch('/api/cart') || [];
            console.log("===> Server cart response:", serverItems);

            // IMPORTANT: Convert each server item to Product instance
            this.items = serverItems.map(item => Product.fromCartItem(item));

            console.log("===> Final cart items:", this.items.map(p => ({
                name: p.name,
                price: p.price,
                quantity: p.quantity,
                total: p.getTotalPrice(),
                type: p.getTypeBadge?.(),
                isGiftCard: p.isGiftCard
            })));

            this.render();
            this.updateBadge();

        } catch (error) {
            console.error('Error loading cart from server:', error);
            this.uiManager.showToast('Error loading cart: ' + error.message, 'error');
        }
    }

    // Main sync method
    async syncWithServer() {
        if (!this.authManager.isAuthenticated) {
            // For unauthenticated users, load from localStorage
            this.loadFromLocalStorage();
            return;
        }

        // For authenticated users, use the smart sync logic
        await this.syncLocalCartWithServer();
    }

    async addItem(productId) {
        try {
            let product;

            if (!this.authManager.isAuthenticated) {
                // For guest users, fetch product details
                const response = await fetch(`/api/products/${productId}`);
                const productData = await response.json();
                product = Product.fromProductResponse(productData);
            }

            if (!this.authManager.isAuthenticated) {
                // Guest user - add to localStorage
                const existingItem = this.items.find(item => item.id === productId);

                if (existingItem) {
                    existingItem.quantity += 1;
                } else {
                    product.quantity = 1;
                    product.cartItemId = `temp-${Date.now()}-${productId}`;
                    this.items.push(product);
                }

                this.saveToLocalStorage();
                this.render();
                this.updateBadge();
                this.wishlistManager?.closeDrawer();
                this.open();
                // this.uiManager.showToast(`${product.name} added to cart!`, 'success');

            } else {
                // Authenticated user - use API
                await this.apiClient.fetch(`/api/cart/add/${productId}`, { method: 'POST' });

                // After adding, load from server (this won't trigger sync again)
                await this.loadFromServer();

                this.wishlistManager?.closeDrawer();
                this.open();
                // this.uiManager.showToast(`${product.name} added to cart!`, 'success');
            }
        } catch (error) {
            console.error('Error adding item to cart:', error);
            this.uiManager.showToast('Error adding item to cart', 'error');
        }
    }

    async addGiftCard(amount) {
        amount = parseFloat(amount);
        if (isNaN(amount) || amount < 10 || amount > 500) {
            this.uiManager.showToast("Please enter a valid amount between $10 and $500", "error", 4000);
            return;
        }

        amount = Math.round(amount / 5) * 5;

        const giftCardData = {
            productId: null,
            name: `Gift Card`,
            price: amount,
            quantity: 1,
            isGiftCard: true,
            recipientEmail: this.authManager.isAuthenticated ? this.authManager.user.email : null,
            message: "",
            imageUrl: 'https://placehold.co/600x400/9333ea/ffffff?text=Gift+Card',
            stock: -1,
            category: 'Gift Card'
        };

        if (!this.authManager.isAuthenticated) {
            const giftCardProduct = new Product(giftCardData);
            this.items.push(giftCardProduct);
            this.saveToLocalStorage();
            this.render();
            this.open();
            // this.uiManager.showToast(`$${amount} Gift Card added to cart!`, "success", 3000);
        } else {
            try {
                await this.apiClient.fetch('/api/cart/add-gift-card', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        amount: amount,
                        recipientEmail: this.authManager.user.email,
                        message: ""
                    })
                });
                await this.loadFromServer();
                this.open();
                // this.uiManager.showToast(`$${amount} Gift Card added to cart!`, "success", 3000);
            } catch (error) {
                console.error('Error adding gift card:', error);
                this.uiManager.showToast('Error adding gift card: ' + error.message, 'error');
            }
        }
    }

    async removeItem(event, itemId) {
        if (!this.authManager.isAuthenticated) {
            const index = this.items.findIndex(item => item.isGiftCard === true || item.id == itemId.split("-")[2]);
            console.log("====> ", index);
            if (index !== -1) {
                const item = this.items[index];
                this.items.splice(index, 1);
                this.saveToLocalStorage();
                this.render();
                this.updateBadge();
                this.uiManager.showToast(`${item.name} removed from cart`, 'success');
            }
            return;
        }

        try {
            this.uiManager.showSpinner(event, itemId);
            await this.apiClient.fetch(`/api/cart/remove/${itemId}`, { method: 'DELETE' });
            await this.loadFromServer();
        } catch (error) {
            this.uiManager.showToast('Error removing item from cart', 'error');
        }
    }

    async clear() {
        if (!await this.uiManager.confirm('Clear Cart', 'Are you sure you want to empty your cart?')) return;

        if (!this.authManager.isAuthenticated) {
            this.items = [];
            this.clearLocalStorage();
            this.render();
            this.updateBadge();
            this.uiManager.showToast('Cart cleared', 'success');
            return;
        }

        try {
            await this.apiClient.fetch('/api/cart/clear', { method: 'DELETE' });
            await this.loadFromServer();
        } catch (error) {
            this.uiManager.showToast('Error clearing cart: ' + error.message, 'error');
        }
    }

    // Helper method to get product details
    async getProductDetails(productId) {
        try {
            const response = await fetch(`/api/products/${productId}`);
            return await response.json();
        } catch (error) {
            console.error('Error fetching product details:', error);
            return null;
        }
    }

    toggle() {
        this.isOpen = !this.isOpen;
        const drawer = document.getElementById('cart-drawer');
        const overlay = document.getElementById('cart-overlay');

        if (this.isOpen) {
            drawer.classList.remove('translate-x-full');
            overlay.classList.remove('hidden');
            setTimeout(() => overlay.classList.add('opacity-100'), 10);
            this.render();
        } else {
            drawer.classList.add('translate-x-full');
            overlay.classList.remove('opacity-100');
            setTimeout(() => overlay.classList.add('hidden'), 300);
        }
    }

    open() {
        if (!this.isOpen) this.toggle();
    }

    close() {
        if (this.isOpen) this.toggle();
    }

    getTotal() {
        return this.items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    }

    getItemCount() {
        return this.items.reduce((sum, item) => sum + item.quantity, 0);
    }

    render() {
        const container = document.getElementById('cart-items-list');
        const totalEl = document.getElementById('cart-total');

        if (!this.items || this.items.length === 0) {
            container.innerHTML = '<p class="text-gray-500 text-center mt-10 font-medium">Your cart is lonely...</p>';
            totalEl.innerText = '$0.00';
            this.updateBadge();
            return;
        }

        console.log("-----> ", this.items);

        const html = this.items.map(product => {
            const imageUrl = product.isGiftCard
                ? `https://placehold.co/600x400/9333ea/ffffff?text=Gift+Card+${product.price}`
                : (product.imageUrl || 'https://placehold.co/600x400/EEE/31343C');

            return `
            <div class="flex items-center space-x-4 border-b border-gray-100 pb-4 group relative">
                <div class="flex-shrink-0 w-16 h-16 bg-gray-50 rounded-lg overflow-hidden">
                    <img src="${imageUrl}" 
                        alt="${product.name}" 
                        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300">
                    <div class="absolute inset-0 flex items-center justify-center bg-white/70 hidden" id="spinner-${product.cartItemId}">
                        <svg class="animate-spin h-6 w-6 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"> 
                            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle> 
                            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path> 
                        </svg> 
                    </div>
                </div>
                <div class="flex-grow">
                    <div class="flex items-center gap-2">
                        <h4 class="font-bold text-sm text-gray-800 line-clamp-1">${product.name}</h4>
                    </div>
                    <div class="flex items-center space-x-2 mt-1">
                        <span class="text-blue-600 font-semibold text-xs">${product.getFormattedPrice()}</span>
                        <span class="text-gray-400 text-[10px]">x ${product.quantity}</span>
                    </div>
                    ${!this.authManager.isAuthenticated ? '<span class="text-xs text-amber-600">Guest item</span>' : ''}
                </div>
                <div class="flex flex-col items-end space-y-2">
                    <span class="font-bold text-sm text-gray-900">$${product.getTotalPrice()}</span>
                    <button onclick="window.cartManager.removeItem(event, '${product.cartItemId}')" class="text-gray-400 hover:text-red-500 transition-colors p-1">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                        </svg>
                    </button>
                </div>
            </div>`;
        }).join('');

        container.innerHTML = html;
        totalEl.innerText = `$${this.getTotal().toFixed(2)}`;
        this.updateBadge();
    }

    updateBadge() {
        let count = this.getItemCount();
        localStorage.setItem('cartCount', JSON.stringify(count));
        this.uiManager.updateCartBadge(count);
    }
}