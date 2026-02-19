import { ComponentStore } from '../core/ComponentStore.js';
import { UIManager } from './UIManager.js';
import { CartManager } from './CartManager.js';
import { Constants } from '../config/Constants.js';

export class OrderManager {
    static instance = null;

    constructor(apiClient) {
        this.apiClient = apiClient;
        this.componentStore = ComponentStore.getInstance();
        this.uiManager = UIManager.getInstance();
        this.cartManager = CartManager.getInstance(apiClient);
        this.statusColors = Constants.STATUS_COLORS;
    }

    static getInstance(apiClient) {
        if (!OrderManager.instance) {
            OrderManager.instance = new OrderManager(apiClient);
        }
        return OrderManager.instance;
    }

    async placeOrder() {
        if (this.cartManager.items.length === 0) {
            this.uiManager.showToast('Your cart is empty!', 'error');
            return false;
        }

        try {
            await this.apiClient.fetch('/api/orders/place', { method: 'POST' });
            await this.cartManager.syncWithServer();
            this.showOrderSuccess();
            return true;
        } catch (error) {
            this.uiManager.showToast('Order Creation Failed: ' + error.message, 'error');
            return false;
        }
    }

    showOrderSuccess() {
        const content = document.getElementById('content');
        content.innerHTML = `
            <div class="text-center py-20 animate-fade-in">
                <div class="bg-green-100 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
                    <svg class="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path>
                    </svg>
                </div>
                <h2 class="text-4xl font-black text-gray-900 mb-4">Success!</h2>
                <p class="text-gray-600 text-lg mb-8">Your order has been received. Transaction ID: #FAKE-${Math.floor(Math.random() * 100000000)}</p>
                <button onclick="window.router.navigate('/products')" class="bg-blue-600 text-white px-8 py-3 rounded-xl font-bold hover:bg-black transition">
                    Back to Shop
                </button>
            </div>
        `;
    }

    async getMyOrders() {
        try {
            return await this.apiClient.fetch('/api/orders/my-orders') || [];
        } catch (error) {
            this.uiManager.showToast('Error loading orders: ' + error.message, 'error');
            return [];
        }
    }

    async renderOrders() {
        const orders = await this.getMyOrders();
        const template = await this.componentStore.load('orders');

        if (orders.length === 0) {
            return template.replace('{{orderList}}', '<p class="text-center py-10 text-gray-500">No orders found yet.</p>');
        }

        const ordersHtml = orders.map(order => `
            <div class="bg-white border rounded-2xl p-6 shadow-sm mb-6">
                <div class="flex justify-between items-center border-b pb-4 mb-4">
                    <div>
                        <p class="text-xs text-gray-400 uppercase font-bold">Order ID</p>
                        <p class="font-mono text-sm">#ORD-${order.id}</p>
                    </div>
                    <div class="text-right">
                        <p class="text-xs text-gray-400 uppercase font-bold">Date</p>
                        <p class="text-sm">${new Date(order.orderDate).toLocaleDateString()}</p>
                    </div>
                </div>
                <div class="space-y-3">
                    ${order.items.map(item => `
                        <div class="flex justify-between text-sm">
                            <span class="text-gray-600">${item.productName} (x${item.quantity})</span>
                            <span class="font-medium">$${(item.priceAtPurchase * item.quantity).toFixed(2)}</span>
                        </div>
                    `).join('')}
                </div>
                <div class="border-t mt-4 pt-4 flex justify-between items-center">
                    <span class="font-bold text-lg text-gray-900">Total Amount</span>
                    <span class="text-2xl font-black text-blue-600">$${order.totalAmount.toFixed(2)}</span>
                </div>
            </div>
        `).join('');

        return template.replace('{{orderList}}', ordersHtml);
    }
}