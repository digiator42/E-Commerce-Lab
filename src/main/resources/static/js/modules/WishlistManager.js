import { UIManager } from './UIManager.js';
import { AuthManager } from './AuthManager.js';

export class WishlistManager {
    static instance = null;

    constructor(apiClient) {
        this.apiClient = apiClient;
        this.uiManager = UIManager.getInstance();
        this.authManager = AuthManager.getInstance();
        this.items = [];
        this.isOpen = false;
        this.storageKey = 'guest_wishlist';
        this.syncCompletedKey = 'wishlist_sync_completed';
    }

    static getInstance(apiClient) {
        if (!WishlistManager.instance) {
            WishlistManager.instance = new WishlistManager(apiClient);
        }
        return WishlistManager.instance;
    }

    // Check if user has local wishlist items
    hasLocalWishlistItems() {
        try {
            const savedWishlist = localStorage.getItem(this.storageKey);
            return savedWishlist && JSON.parse(savedWishlist).length > 0;
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

    // Load wishlist from localStorage for guest users
    loadFromLocalStorage() {
        try {
            const savedWishlist = localStorage.getItem(this.storageKey);
            if (savedWishlist) {
                this.items = JSON.parse(savedWishlist);
                console.log('Loaded wishlist from localStorage:', this.items.map(i => i.name));
            } else {
                this.items = [];
            }
            this.updateBadge();
            this.renderDrawer();
        } catch (error) {
            console.error('Error loading wishlist from localStorage:', error);
            this.items = [];
        }
    }

    // Save wishlist to localStorage for guest users
    saveToLocalStorage() {
        try {
            // Store plain objects in localStorage
            const itemsToStore = this.items.map(item => ({
                id: item.id,
                name: item.name,
                price: item.price,
                imageUrl: item.imageUrl,
                category: item.category
            }));

            localStorage.setItem(this.storageKey, JSON.stringify(itemsToStore));
            console.log('Saved wishlist to localStorage:', itemsToStore);
        } catch (error) {
            console.error('Error saving wishlist to localStorage:', error);
        }
    }

    // Clear localStorage wishlist
    clearLocalStorage() {
        localStorage.removeItem(this.storageKey);
        console.log('Cleared localStorage wishlist');
    }

    // Sync local wishlist with server after login
    async syncLocalWishlistWithServer() {
        // Don't sync if not authenticated
        if (!this.authManager.isAuthenticated) return;

        // Load current local items FIRST to check
        this.loadFromLocalStorage();

        // Check if we have local items to sync
        const hasLocalItems = this.items.length > 0;

        // Check if sync was already completed for this user
        const syncCompleted = this.wasSyncCompleted();

        console.log('Wishlist sync check:', {
            hasLocalItems,
            syncCompleted,
            authenticated: this.authManager.isAuthenticated,
            items: this.items.map(i => i.name)
        });

        // If sync already completed for this user, just load from server
        if (syncCompleted) {
            console.log('Wishlist sync already completed for this user, loading from server');
            await this.loadFromServer();
            return;
        }

        // If no local items, just load from server and mark sync as completed
        if (!hasLocalItems) {
            console.log('No local wishlist items, loading from server');
            await this.loadFromServer();
            this.markSyncCompleted();
            return;
        }

        // We have local items and sync not completed - perform sync
        console.log('Syncing local wishlist with server:', this.items);

        try {
            // Add each item from local wishlist to server
            for (const item of this.items) {
                try {
                    await this.apiClient.fetch(`/api/wishlist/${item.id}`, {
                        method: 'POST'
                    });
                } catch (itemError) {
                    console.error(`Error adding item ${item.id} to wishlist:`, itemError);
                    // Continue with other items even if one fails
                }
            }

            // Clear local wishlist AFTER successful sync
            this.clearLocalStorage();
            this.items = []; // Clear in-memory items

            // Mark sync as completed for this user
            this.markSyncCompleted();

            // Load updated wishlist from server
            await this.loadFromServer();

            this.uiManager.showToast('Your wishlist has been synced with your account!', 'success');

        } catch (error) {
            console.error('Error syncing local wishlist with server:', error);
            this.uiManager.showToast('Error syncing your wishlist. Please try again.', 'error');
        }
    }

    async loadFromServer() {
        if (!this.authManager.isAuthenticated) return;

        try {
            this.items = await this.apiClient.fetch('/api/wishlist') || [];
            console.log("===> Server wishlist response:", this.items);
            this.updateBadge();
            this.renderDrawer();
            return this.items;
        } catch (error) {
            console.error('Error loading wishlist from server:', error);
            this.uiManager.showToast('Error loading wishlist: ' + error.message, 'error');
        }
    }

    // Main sync method - called on route changes
    async syncWithServer() {
        if (!this.authManager.isAuthenticated) {
            // For unauthenticated users, load from localStorage
            this.loadFromLocalStorage();
            return;
        }

        // For authenticated users, use the smart sync logic
        await this.syncLocalWishlistWithServer();
    }

    async addItem(productId) {
        // For guest users, we need to fetch product details
        if (!this.authManager.isAuthenticated) {
            try {
                // Check if item already exists in wishlist
                if (this.items.some(item => item.id === productId)) {
                    this.uiManager.showToast('Item already in wishlist', 'info');
                    return false;
                }

                // Fetch product details
                const productData = await this.apiClient.fetch(`/api/products/${productId}`);

                // Add to local wishlist
                const wishlistItem = {
                    id: productData.id,
                    name: productData.name,
                    price: productData.price,
                    imageUrl: productData.imageUrl,
                    category: productData.category
                };

                this.items.push(wishlistItem);
                this.saveToLocalStorage();
                this.updateBadge();
                this.renderDrawer();
                this.uiManager.showToast('Added to wishlist!', 'success');

                // Update heart icon if visible
                this.updateHeartIcon(productId, true);

                return true;
            } catch (error) {
                console.error('Error adding item to guest wishlist:', error);
                this.uiManager.showToast('Error adding to wishlist', 'error');
                return false;
            }
        }

        // Authenticated user flow
        try {
            await this.apiClient.fetch(`/api/wishlist/${productId}`, {
                method: 'POST'
            });

            await this.loadFromServer();
            this.uiManager.showToast('Added to wishlist!', 'success');

            // Update heart icon if visible
            this.updateHeartIcon(productId, true);

            return true;
        } catch (error) {
            this.uiManager.showToast('Error adding to wishlist: ' + error.message, 'error');
            return false;
        }
    }

    async removeItem(event, productId) {
        // Guest user flow
        if (!this.authManager.isAuthenticated) {
            const index = this.items.findIndex(item => item.id === productId);
            if (index !== -1) {
                const item = this.items[index];
                this.items.splice(index, 1);
                this.saveToLocalStorage();
                this.updateBadge();
                this.renderDrawer();

                // Update heart icon if visible
                this.updateHeartIcon(productId, false);
            }
            return true;
        }

        // Authenticated user flow
        if (!this.authManager.isAuthenticated) return false;

        if (event) {
            this.uiManager.showSpinner(event, productId);
        }

        try {
            await this.apiClient.fetch(`/api/wishlist/${productId}`, {
                method: 'DELETE'
            });

            this.items = this.items.filter(item => item.id !== productId);
            this.updateBadge();

            // Update heart icon if visible
            this.updateHeartIcon(productId, false);

            // Refresh from server to ensure consistency
            if (this.authManager.isAuthenticated) {
                await this.loadFromServer();
            } else {
                this.renderDrawer();
            }

            return true;
        } catch (error) {
            this.uiManager.showToast('Error removing from wishlist: ' + error.message, 'error');
            return false;
        }
    }

    async toggleItem(event, productId) {

        const heartIcon = event.currentTarget.querySelector('svg');

        if (this.isInWishlist(productId)) {
            // Remove from wishlist
            await this.removeItem(event, productId);
        } else {
            // Add to wishlist
            await this.addItem(productId);
        }
    }

    isInWishlist(productId) {
        return this.items.some(item => item.id === productId);
    }

    updateHeartIcon(productId, isInWishlist) {
        const heartIcon = document.querySelector(`svg[data-product-id="${productId}"]`);
        
        if (heartIcon) {
            if (isInWishlist) {
                heartIcon.classList.add('text-red-500', 'fill-current');
                heartIcon.classList.remove('text-gray-400');
            } else {
                heartIcon.classList.remove('text-red-500', 'fill-current');
                heartIcon.classList.add('text-gray-400');
            }
        }
    }

    updateBadge() {
        const badge = document.getElementById('wishlist-count-desktop');
        const mobileBadge = document.getElementById('wishlist-count-mobile');

        localStorage.setItem('wishlistCount', JSON.stringify(this.getItemCount()));

        [badge, mobileBadge].forEach(b => {
            if (b) {
                const count = this.getItemCount();
                b.innerText = count;
                count > 0 ? b.classList.remove('hidden') : b.classList.add('hidden');
            }
        });
    }

    getItemCount() {
        return this.items.length;
    }

    toggleDrawer() {
        this.isOpen = !this.isOpen;
        const drawer = document.getElementById('wishlist-drawer');
        const overlay = document.getElementById('wishlist-overlay');

        if (this.isOpen) {
            drawer.classList.remove('translate-x-full');
            overlay.classList.remove('hidden');
            setTimeout(() => overlay.classList.add('opacity-100'), 10);
            this.renderDrawer();
        } else {
            drawer.classList.add('translate-x-full');
            overlay.classList.remove('opacity-100');
            setTimeout(() => overlay.classList.add('hidden'), 300);
        }
    }

    closeDrawer() {
        if (this.isOpen) this.toggleDrawer();
    }

    renderDrawer() {
        const container = document.getElementById('wishlist-items-list');

        if (!this.items || this.items.length === 0) {
            container.innerHTML = `
            <div class="text-center py-10">
                <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                </svg>
                <p class="text-gray-500 font-medium">Your wishlist is empty</p>
                <p class="text-sm text-gray-400 mt-1">Save items you love ❤️</p>
                ${!this.authManager.isAuthenticated ? '<p class="text-xs text-amber-600 mt-2">Sign in to save your wishlist permanently</p>' : ''}
            </div>
        `;
            return;
        }

        container.innerHTML = this.items.map(item => `
        <div class="flex items-center space-x-4 border-b border-gray-100 pb-4 group relative" id="item-${item.id}">

            <div class="absolute inset-0 flex items-center justify-center bg-white/70 hidden" id="spinner-${item.id}">
                <svg class="animate-spin h-6 w-6 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"> 
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle> 
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path> 
                </svg> 
            </div>

            <div class="flex-shrink-0 w-16 h-16 bg-gray-50 rounded-lg overflow-hidden">
                <img src="${item.imageUrl || 'https://placehold.co/600x400/EEE/31343C'}" 
                    alt="${item.name}" 
                    class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300">
            </div>
            <div class="flex-grow">
                <h4 class="font-bold text-sm text-gray-800 line-clamp-1">${item.name}</h4>
                <div class="flex items-center mt-1">
                    <span class="text-blue-600 font-semibold text-xs">$${item.price?.toFixed(2) || '0.00'}</span>
                </div>
                ${!this.authManager.isAuthenticated ? '<span class="text-xs text-amber-600">Guest item</span>' : ''}
            </div>
            <div class="flex flex-col items-end space-y-2">
                <button onclick="window.wishlistManager.removeItem(event, ${item.id})" 
                    class="text-gray-400 hover:text-red-500 transition-colors p-1">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                              d="M6 18L18 6M6 6l12 12"></path>
                    </svg>
                </button>
                <button onclick="window.cartManager.addItem(${item.id})" 
                    class="text-xs bg-blue-50 text-blue-600 px-2 py-1 rounded-lg hover:bg-blue-100 transition">
                    Add to Cart
                </button>
            </div>
        </div>
    `).join('');
    }
}