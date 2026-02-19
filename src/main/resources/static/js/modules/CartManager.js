import { UIManager } from './UIManager.js';
import { AuthManager } from './AuthManager.js';

export class CartManager {
    static instance = null;

    constructor(apiClient) {
        this.items = [];
        this.isOpen = false;
        this.apiClient = apiClient;
        this.authManager = AuthManager.getInstance();
        this.uiManager = UIManager.getInstance();
    }

    static getInstance(apiClient) {
        if (!CartManager.instance) {
            CartManager.instance = new CartManager(apiClient);
        }
        return CartManager.instance;
    }

    async syncWithServer() {
        if (!this.authManager.isAuthenticated) return;

        try {
            this.items = await this.apiClient.fetch('/api/cart') || [];
            this.render();
            this.updateBadge();
        } catch (error) {
            console.error('Error syncing cart:', error);
            this.uiManager.showToast('Error syncing cart: ' + error.message, 'error');
        }
    }

    async addItem(productId) {
        try {
            await this.apiClient.fetch(`/api/cart/add/${productId}`, { method: 'POST' });
            await this.syncWithServer();
            this.open();
        } catch (error) {
            this.uiManager.showToast('Error adding item to cart', 'error');
        }
    }

    async removeItem(cartItemId) {
        try {
            await this.apiClient.fetch(`/api/cart/remove/${cartItemId}`, { method: 'DELETE' });
            await this.syncWithServer();
        } catch (error) {
            this.uiManager.showToast('Error removing item from cart', 'error');
        }
    }

    async clear() {
        if (!confirm('Are you sure you want to empty your cart?')) return;

        try {
            await this.apiClient.fetch('/api/cart/clear', { method: 'DELETE' });
            this.items = [];
            this.render();
            this.updateBadge();
        } catch (error) {
            this.uiManager.showToast('Error clearing cart: ' + error.message, 'error');
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

        const html = this.items.map(item => {
            const itemTotal = (item.price * item.quantity).toFixed(2);
            return `
                <div class="flex items-center space-x-4 border-b border-gray-100 pb-4 group">
                    <div class="flex-shrink-0 w-16 h-16 bg-gray-50 rounded-lg overflow-hidden">
                        <img src="${item.imageUrl || 'https://placehold.co/600x400/EEE/31343C'}" 
                            alt="${item.name}" 
                            class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300">
                    </div>
                    <div class="flex-grow">
                        <h4 class="font-bold text-sm text-gray-800 line-clamp-1">${item.name}</h4>
                        <div class="flex items-center space-x-2 mt-1">
                            <span class="text-blue-600 font-semibold text-xs">$${item.price.toFixed(2)}</span>
                            <span class="text-gray-400 text-[10px]">x ${item.quantity}</span>
                        </div>
                    </div>
                    <div class="flex flex-col items-end space-y-2">
                        <span class="font-bold text-sm text-gray-900">$${itemTotal}</span>
                        <button onclick="window.cartManager.removeItem(${item.id})" class="text-gray-400 hover:text-red-500 transition-colors p-1">
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
        this.uiManager.updateCartBadge(this.getItemCount());
    }
}