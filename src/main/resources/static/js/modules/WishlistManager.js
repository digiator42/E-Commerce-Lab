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
    }

    static getInstance(apiClient) {
        if (!WishlistManager.instance) {
            WishlistManager.instance = new WishlistManager(apiClient);
        }
        return WishlistManager.instance;
    }

    async syncWithServer() {
        if (!this.authManager.isAuthenticated) {
            this.items = [];
            this.updateBadge();
            return;
        }

        try {
            this.items = await this.apiClient.fetch('/api/wishlist') || [];
            this.updateBadge();
            this.renderDrawer();
            return this.items;
        } catch (error) {
            console.error('Error syncing wishlist:', error);
            this.uiManager.showToast('Error syncing wishlist: ' + error.message, 'error');
        }
    }

    async addItem(productId) {
        if (!this.authManager.isAuthenticated) {
            this.uiManager.showToast('Please login to add items to wishlist', 'error');
            window.router.navigate('/login');
            return false;
        }

        try {
            await this.apiClient.fetch(`/api/wishlist/${productId}`, {
                method: 'POST'
            });

            await this.syncWithServer();
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
        if (!this.authManager.isAuthenticated) return false;

        try {
            await this.apiClient.fetch(`/api/wishlist/${productId}`, {
                method: 'DELETE'
            });

            this.items = this.items.filter(item => item.id !== productId);
            this.updateBadge();
            this.uiManager.showToast('Removed from wishlist', 'success');
            this.uiManager.showSpinner(event, productId);

            // Update heart icon if visible
            this.updateHeartIcon(productId, false);

            this.syncWithServer(); // Refresh the list after removal

            return true;
        } catch (error) {
            this.uiManager.showToast('Error removing from wishlist: ' + error.message, 'error');
            return false;
        }
    }

    async toggleItem(productId) {
        if (this.isInWishlist(productId)) {
            await this.removeItem(productId);
        } else {
            await this.addItem(productId);
        }
    }

    isInWishlist(productId) {
        return this.items.some(item => item.id === productId);
    }

    updateHeartIcon(productId, isInWishlist) {
        const heartIcon = document.querySelector(`.wishlist-heart[data-product-id="${productId}"]`);
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
        const badge = document.getElementById('wishlist-count');
        if (badge) {
            const count = this.items.length;
            badge.innerText = count;
            count > 0 ? badge.classList.remove('hidden') : badge.classList.add('hidden');
        }
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
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                    </svg>
                    <p class="text-gray-500 font-medium">Your wishlist is empty</p>
                    <p class="text-sm text-gray-400 mt-1">Save items you love ❤️</p>
                </div>
            `;
            return;
        }

        container.innerHTML = this.items.map(item => `
            <div class="flex items-center space-x-4 border-b border-gray-100 pb-4 group">
                <div class="flex-shrink-0 w-16 h-16 bg-gray-50 rounded-lg overflow-hidden">
                    <img src="${item.imageUrl || 'https://placehold.co/600x400/EEE/31343C'}" 
                        alt="${item.name}" 
                        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300">
                    <div class="absolute inset-0 flex items-center justify-center bg-white/70 hidden" id="spinner-${item.id}"> 
                        <svg class="animate-spin h-6 w-6 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"> 
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle> 
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path> </svg> 
                    </div>
                </div>
                <div class="flex-grow">
                    <h4 class="font-bold text-sm text-gray-800 line-clamp-1">${item.name}</h4>
                    <div class="flex items-center mt-1">
                        <span class="text-blue-600 font-semibold text-xs">$${item.price.toFixed(2)}</span>
                    </div>
                </div>
                <div class="flex flex-col items-end space-y-2">
                    <button onclick="window.wishlistManager.removeItem(event, ${item.id})" 
                        class="text-gray-400 hover:text-red-500 transition-colors p-1">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
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