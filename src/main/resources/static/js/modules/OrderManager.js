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

    async checkout(event) {
        const checkoutBtn = event.target;
        if (this.cartManager.items.length === 0) return this.uiManager.showToast("Your cart is empty!", "error", 5000);

        checkoutBtn.disabled = true;
        checkoutBtn.innerText = "Processing...";

        this.openPaymentModal();
    }

    openPaymentModal() {
        const total = this.cartManager.items.reduce((sum, item) => sum + (item.price * item.quantity), 0).toFixed(2);
        this.cartManager.toggle();
        const modalHtml = `
        <div id="payment-overlay" class="fixed inset-0 bg-black/50 backdrop-blur-sm z-40 flex items-center justify-center p-4">
            <div class="bg-white w-full max-w-md rounded-3xl p-8 shadow-2xl scale-in-center">
                <h3 class="text-2xl font-black mb-6">Fake Secure Payment</h3>
                <div class="space-y-4">
                    <div class="bg-gray-100 p-4 rounded-2xl flex justify-between">
                        <span class="text-gray-500">Total to Pay</span>
                        <span class="font-black text-blue-600">$${total}</span>
                    </div>
                    <input type="text" placeholder="Card Number" class="w-full p-4 bg-gray-50 rounded-xl border-none ring-1 ring-gray-200 focus:ring-2 focus:ring-blue-500">
                    <div class="grid grid-cols-2 gap-4">
                        <input type="text" placeholder="MM/YY" class="p-4 bg-gray-50 rounded-xl border-none ring-1 ring-gray-200">
                        <input type="text" id="card-cvv" placeholder="CVV (3 digits)" class="p-4 bg-gray-50 rounded-xl border-none ring-1 ring-gray-200">
                    </div>
                </div>
                <div class="mt-8 flex gap-4">
                    <button onclick="window.orderManager.closePaymentModal()" class="flex-1 font-bold text-gray-400">Cancel</button>
                    <button onclick="window.orderManager.processFakePayment()" id="pay-now-btn" class="flex-1 bg-black text-white py-4 rounded-2xl font-bold hover:bg-blue-600 transition">
                        Pay Now
                    </button>
                </div>
            </div>
        </div>
    `;
        document.body.insertAdjacentHTML('beforeend', modalHtml);
    }

    closePaymentModal() {
        const modal = document.getElementById('payment-overlay');
        if (modal) modal.remove();
        const checkoutBtn = document.getElementById('checkout-btn');

        checkoutBtn.disabled = false;
        checkoutBtn.innerText = "Checkout";
    }

    async processFakePayment() {
        const btn = document.getElementById('pay-now-btn');
        const cvv = document.getElementById('card-cvv').value;

        btn.innerText = "Verifying...";
        btn.disabled = true;

        await new Promise(resolve => setTimeout(resolve, 2000));

        if (cvv === "000") {
            this.uiManager.showToast("Payment Declined: Insufficient Funds", "error", 5000);
            btn.innerText = "Try Again";
            btn.disabled = false;
            return;
        }

        await this.finalizeCheckout();
    }

    async finalizeCheckout() {

        const checkoutBtn = document.getElementById('checkout-btn');

        try {
            const response = await this.apiClient.fetch('/api/orders/place', { method: 'POST' });

            this.cartManager.toggle();

            document.getElementById('content').innerHTML = `
            <div class="text-center py-20 animate-fade-in">
                <div class="bg-green-100 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
                    <svg class="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path></svg>
                </div>
                <h2 class="text-4xl font-black text-gray-900 mb-4">Success!</h2>
                <p class="text-gray-600 text-lg mb-8">Your order has been received. Transaction ID: #FAKE-${Math.floor(Math.random() * 100000000)}</p>
                <button onclick="window.router.navigate('/products')" class="bg-blue-600 text-white px-8 py-3 rounded-xl font-bold hover:bg-black transition">
                    Back to Shop
                </button>
            </div>
        `;

            this.closePaymentModal();
            await this.cartManager.syncWithServer();

            checkoutBtn.disabled = false;
            checkoutBtn.innerText = "Checkout";
        } catch (err) {
            this.uiManager.showToast("Order Creation Failed: " + err.message, "error", 5000);
            this.closePaymentModal();
            await this.cartManager.syncWithServer();
            checkoutBtn.disabled = false;
            checkoutBtn.innerText = "Checkout";
        }
    }

    async getMyOrders() {
        try {
            return await this.apiClient.fetch('/api/orders/my-orders') || [];
        } catch (error) {
            this.uiManager.showToast('Error loading orders: ' + error.message, 'error', 5000);
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