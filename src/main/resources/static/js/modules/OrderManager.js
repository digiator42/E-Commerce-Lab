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
        this.appliedCoupon = null;
        this.discountedTotal = null;
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

        // Reset coupon state when opening new checkout
        this.appliedCoupon = null;
        this.discountedTotal = null;

        this.openPaymentModal();
    }

    // Update openPaymentModal to include coupon section
    openPaymentModal() {
        const subtotal = this.cartManager.items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        const subtotalFormatted = subtotal.toFixed(2);

        const modalHtml = `
    <div id="payment-overlay" class="fixed inset-0 bg-black/50 backdrop-blur-sm z-40 flex items-center justify-center p-4">
        <div class="bg-white w-full max-w-md rounded-3xl p-8 shadow-2xl scale-in-center">
            <h3 class="text-2xl font-black mb-6">Fake Secure Payment</h3>
            
            <!-- Coupon Section -->
            <div class="mb-6 p-4 bg-gray-50 rounded-2xl">
                <div class="flex gap-2 mb-3">
                    <input type="text" 
                           id="coupon-code" 
                           placeholder="Have a coupon?" 
                           class="flex-1 p-3 bg-white rounded-xl border-none ring-1 ring-gray-200 focus:ring-2 focus:ring-blue-500">
                    <button onclick="window.orderManager.applyCoupon()" 
                            id="apply-coupon-btn"
                            class="px-4 bg-black text-white rounded-xl font-bold hover:bg-blue-600 transition">
                        Apply
                    </button>
                </div>
                <div id="coupon-message" class="text-sm hidden"></div>
                <div id="coupon-details" class="text-sm hidden"></div>
            </div>
            
            <div class="space-y-4">
                <!-- Price Breakdown -->
                <div class="bg-gray-100 p-4 rounded-2xl">
                    <div class="flex justify-between mb-2">
                        <span class="text-gray-500">Subtotal</span>
                        <span class="font-bold">$${subtotalFormatted}</span>
                    </div>
                    <div id="discount-row" class="flex justify-between text-green-600 hidden">
                        <span>Discount</span>
                        <span id="discount-amount">-$0.00</span>
                    </div>
                    <div class="flex justify-between text-lg font-black mt-2 pt-2 border-t border-gray-300">
                        <span>Total to Pay</span>
                        <span id="total-amount" class="text-blue-600">$${subtotalFormatted}</span>
                    </div>
                </div>
                
                <!-- Payment Form -->
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

    // New method to apply coupon
    async applyCoupon() {
        const couponInput = document.getElementById('coupon-code');
        const applyBtn = document.getElementById('apply-coupon-btn');
        const couponMessage = document.getElementById('coupon-message');
        const couponDetails = document.getElementById('coupon-details');
        const code = couponInput.value.trim();

        if (!code) {
            this.showCouponMessage('Please enter a coupon code', 'error');
            return;
        }

        // Disable button while validating
        applyBtn.disabled = true;
        applyBtn.innerText = '...';
        couponMessage.classList.add('hidden');

        try {
            const response = await this.apiClient.fetch(`/api/coupons/check?code=${encodeURIComponent(code)}`);

            const data = await response;

            // Calculate discount
            const subtotal = this.cartManager.items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
            const discountAmount = (subtotal * data.discountPercentage) / 100;
            this.discountedTotal = subtotal - discountAmount;

            // Store applied coupon
            this.appliedCoupon = {
                code: data.code,
                discountPercentage: data.discountPercentage
            };

            // Update UI with discount
            this.updatePriceDisplay(subtotal, discountAmount, data.discountPercentage);

            // Show success message
            this.showCouponMessage(`Coupon applied! You saved $${discountAmount.toFixed(2)}`, 'success');

            // Show coupon details
            couponDetails.innerHTML = `✅ ${data.code} - ${data.discountPercentage}% off`;
            couponDetails.classList.remove('hidden');
            couponDetails.className = 'text-sm text-green-600 font-medium';

            // Clear input
            couponInput.value = '';
        } catch (error) {
            this.showCouponMessage(error.message || 'Invalid coupon code', 'error');
            couponDetails.classList.add('hidden');

            // Reset pricing
            this.resetPriceDisplay();
        } finally {
            // Re-enable button
            applyBtn.disabled = false;
            applyBtn.innerText = 'Apply';
        }
    }

    // Helper method to show coupon messages
    showCouponMessage(message, type) {
        const couponMessage = document.getElementById('coupon-message');
        couponMessage.textContent = message;
        couponMessage.className = `text-sm mt-2 ${type === 'error' ? 'text-red-600' : 'text-green-600'}`;
        couponMessage.classList.remove('hidden');
    }

    closePaymentModal() {
        const modal = document.getElementById('payment-overlay');
        if (modal) modal.remove();
        const checkoutBtn = document.getElementById('checkout-btn');

        checkoutBtn.disabled = false;
        checkoutBtn.innerText = "Checkout";
    }

    // Update price display with discount
    updatePriceDisplay(subtotal, discountAmount, discountPercentage) {
        const discountRow = document.getElementById('discount-row');
        const discountAmountSpan = document.getElementById('discount-amount');
        const totalSpan = document.getElementById('total-amount');

        // Show discount row
        discountRow.classList.remove('hidden');
        discountAmountSpan.textContent = `-$${discountAmount.toFixed(2)} (${discountPercentage}%)`;
        totalSpan.textContent = `$${this.discountedTotal.toFixed(2)}`;
    }

    // Reset price display (remove discount)
    resetPriceDisplay() {
        const discountRow = document.getElementById('discount-row');
        const totalSpan = document.getElementById('total-amount');
        const subtotal = this.cartManager.items.reduce((sum, item) => sum + (item.price * item.quantity), 0);

        discountRow.classList.add('hidden');
        totalSpan.textContent = `$${subtotal.toFixed(2)}`;
        this.discountedTotal = null;
        this.appliedCoupon = null;
    }

    // Update processFakePayment to use discounted total
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

    // Update finalizeCheckout to include coupon code
    async finalizeCheckout() {
        const checkoutBtn = document.getElementById('checkout-btn');

        try {
            // Prepare request body with coupon if applied
            const requestBody = {};
            if (this.appliedCoupon) {
                requestBody.couponCode = this.appliedCoupon.code;
            }

            const response = await this.apiClient.fetch('/api/orders/place', {
                method: 'POST',
                body: JSON.stringify(requestBody),
                headers: {
                    'Content-Type': 'application/json'
                }
            });

        } catch (err) {
            this.uiManager.showToast("Order Creation Failed: " + err.message, "error", 5000);
            this.closePaymentModal();
            await this.cartManager.syncWithServer();
            checkoutBtn.disabled = false;
            checkoutBtn.innerText = "Checkout";
            return;
        }

        // Calculate final total for display
        const subtotal = this.cartManager.items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        const finalTotal = this.discountedTotal || subtotal;
        const savings = this.appliedCoupon ? (subtotal - finalTotal).toFixed(2) : 0;

        this.cartManager.toggle();

        document.getElementById('content').innerHTML = `
        <div class="text-center py-20 animate-fade-in">
            <div class="bg-green-100 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
                <svg class="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path></svg>
            </div>
            <h2 class="text-4xl font-black text-gray-900 mb-4">Success!</h2>
            <p class="text-gray-600 text-lg mb-4">Your order has been received. Transaction ID: #FAKE-${Math.floor(Math.random() * 100000000)}</p>
            ${this.appliedCoupon ? `
            <div class="bg-green-50 p-4 rounded-2xl mb-6 inline-block">
                <p class="text-green-700 font-medium">🎉 You saved $${savings} with coupon ${this.appliedCoupon.code}!</p>
            </div>
            ` : ''}
            <div class="bg-gray-50 p-4 rounded-2xl mb-8 inline-block">
                <p class="text-gray-700">Total paid: <span class="font-black text-blue-600">$${finalTotal.toFixed(2)}</span></p>
            </div>
            <button onclick="window.router.navigate('/products')" class="bg-blue-600 text-white px-8 py-3 rounded-xl font-bold hover:bg-black transition">
                Back to Shop
            </button>
        </div>
    `;

        this.closePaymentModal();
        await this.cartManager.syncWithServer();

        // Reset coupon state
        this.appliedCoupon = null;
        this.discountedTotal = null;

        checkoutBtn.disabled = false;
        checkoutBtn.innerText = "Checkout";

    }

    // Add a method to remove applied coupon (optional)
    removeCoupon() {
        this.appliedCoupon = null;
        this.discountedTotal = null;
        this.resetPriceDisplay();

        const couponDetails = document.getElementById('coupon-details');
        couponDetails.classList.add('hidden');

        this.showCouponMessage('Coupon removed', 'success');
    }

    async getMyOrders() {
        try {
            return await this.apiClient.fetch('/api/orders/my-orders') || [];
        } catch (error) {
            this.uiManager.showToast('Error loading orders: ' + error.message, 'error', 5000);
            return [];
        }
    }

    parseAddress(addressStr) {
        if (!addressStr) return {};
        try {
            return JSON.parse(addressStr);
        } catch (err) {
            return { street: addressStr || 'N/A' };
        }
    };

    async renderOrders() {
        const orders = await this.getMyOrders();
        const template = await this.componentStore.load('orders');

        if (orders.length === 0) {
            return template.replace('{{orderList}}', '<p class="text-center py-10 text-gray-500">No orders found yet.</p>');
        }


        const ordersHtml = orders.map(order => {
            const shippingAddress = this.parseAddress(order.shippingAddress);
            const paymentStatusColor = order.paymentStatus === 'PAID' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700';

            return `
            <div class="bg-white border rounded-2xl p-6 shadow-sm mb-6 hover:shadow-md transition">
                <!-- Order Header -->
                <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center border-b pb-4 mb-4">
                    <div class="space-y-1">
                        <div class="flex items-center space-x-3">
                            <p class="text-xs text-gray-400 uppercase font-bold">Order ID</p>
                            <span class="px-2 py-0.5 rounded-full text-xs font-bold uppercase ${paymentStatusColor}">
                                ${order.paymentStatus}
                            </span>
                        </div>
                        <p class="font-mono text-sm flex items-center">
                            #ORD-${order.id}
                            ${order.paymentTransactionId ? `
                                <span class="ml-2 text-xs text-gray-400 font-mono">
                                    TX: ${order.paymentTransactionId}
                                </span>
                            ` : ''}
                        </p>
                    </div>
                    <div class="flex items-center space-x-4 mt-2 sm:mt-0">
                        <div class="text-right">
                            <p class="text-xs text-gray-400 uppercase font-bold">Date</p>
                            <p class="text-sm">${new Date(order.orderDate).toLocaleDateString()}</p>
                            <p class="text-[10px] text-gray-400">${new Date(order.orderDate).toLocaleTimeString()}</p>
                        </div>
                        <!-- Invoice Download Button -->
                        <a href="/api/orders/${order.id}/download-invoice" 
                           target="_blank"
                           class="inline-flex items-center px-3 py-1.5 bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 transition group"
                           title="Download Invoice">
                            <svg class="w-4 h-4 mr-1 group-hover:scale-110 transition" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                            </svg>
                            <span class="text-xs font-medium">Invoice</span>
                        </a>
                    </div>
                </div>

                <!-- Order Items with Product Details -->
                <div class="space-y-4">
                    <p class="text-xs text-gray-400 uppercase font-bold mb-2">Items</p>
                    ${order.items.map(item => `
                        <div class="flex justify-between items-start border-b border-gray-50 pb-3 last:border-0">
                            <div class="flex-1">
                                <div class="flex items-center">
                                    <span class="font-medium text-gray-900">${item.productName}</span>
                                    ${item.product?.category ? `
                                        <span class="ml-2 text-xs bg-gray-100 px-2 py-0.5 rounded-full">
                                            ${item.product.category.icon || ''} ${item.product.category.name}
                                        </span>
                                    ` : ''}
                                </div>
                                <div class="flex items-center mt-1 text-xs text-gray-500">
                                    <span>Qty: ${item.quantity}</span>
                                    <span class="mx-2">•</span>
                                    <span>Unit Price: $${item.priceAtPurchase.toFixed(2)}</span>
                                    ${item.product?.brand ? `<span class="mx-2">•</span><span>Brand: ${item.product.brand}</span>` : ''}
                                </div>
                            </div>
                            <div class="text-right">
                                <span class="font-bold text-gray-900">$${(item.priceAtPurchase * item.quantity).toFixed(2)}</span>
                            </div>
                        </div>
                    `).join('')}
                </div>

                <!-- Shipping Address -->
                <div class="mt-4 p-3 bg-gray-50 rounded-xl">
                    <p class="text-xs text-gray-400 uppercase font-bold mb-2">Shipping Address</p>
                    <div class="text-sm">
                        <p class="font-medium text-gray-900">${shippingAddress.street || 'N/A'}</p>
                        <p class="text-xs text-gray-600">
                            ${shippingAddress.city || ''}${shippingAddress.city && shippingAddress.state ? ', ' : ''}${shippingAddress.state || ''} ${shippingAddress.zipCode || ''}
                        </p>
                        <p class="text-xs text-gray-600">${shippingAddress.country || ''}</p>
                    </div>
                </div>

                <!-- Order Summary -->
                <div class="border-t mt-4 pt-4">
                    <div class="flex justify-end">
                        <div class="w-full sm:w-64 space-y-2">
                            <div class="flex justify-between text-sm">
                                <span class="text-gray-500">Subtotal:</span>
                                <span class="font-medium">$${order.totalAmount.toFixed(2)}</span>
                            </div>
                            <div class="flex justify-between text-sm">
                                <span class="text-gray-500">Shipping:</span>
                                <span class="font-medium text-green-600">Free</span>
                            </div>
                            <div class="flex justify-between text-lg font-bold border-t pt-2">
                                <span>Total:</span>
                                <span class="text-blue-600">$${order.totalAmount.toFixed(2)}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Order Footer with Status and Actions -->
                <div class="border-t mt-4 pt-4 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
                    <div class="flex items-center space-x-3">
                        <!-- Status Badge -->
                        <span class="px-3 py-1.5 rounded-full text-xs font-bold uppercase ${this.statusColors[order.status] || 'bg-gray-100 text-gray-600'}">
                            ${order.status || 'PENDING'}
                        </span>
                        <!-- Payment Status -->
                        <span class="text-xs ${order.paymentStatus === 'PAID' ? 'text-green-600' : 'text-yellow-600'}">
                            ${order.paymentStatus === 'PAID' ? '✓ Paid' : '⏳ Pending'}
                        </span>
                    </div>
                    <div class="flex items-center space-x-3">
                        <!-- Download again link -->
                        <a href="/api/orders/${order.id}/download-invoice" 
                           target="_blank"
                           class="text-xs text-gray-400 hover:text-blue-600 flex items-center">
                            <svg class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
                            </svg>
                            Download PDF
                        </a>
                        <!-- Track Order (placeholder) -->
                        <button class="text-xs text-gray-400 hover:text-blue-600 flex items-center">
                            <svg class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"></path>
                            </svg>
                            Track Order
                        </button>
                    </div>
                </div>
            </div>
        `;
        }).join('');

        return template.replace('{{orderList}}', ordersHtml);
    }
}