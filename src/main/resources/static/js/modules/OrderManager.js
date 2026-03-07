import { ComponentStore } from '../core/ComponentStore.js';
import { UIManager } from './UIManager.js';
import { CartManager } from './CartManager.js';
import { Constants } from '../config/Constants.js';
import { Router } from '../core/Router.js';

export class OrderManager {
    static instance = null;

    constructor(apiClient) {
        this.apiClient = apiClient;
        this.componentStore = ComponentStore.getInstance();
        this.uiManager = UIManager.getInstance();
        this.cartManager = CartManager.getInstance(apiClient);
        this.router = Router.getInstance();
        this.statusColors = Constants.STATUS_COLORS;
        this.appliedCoupon = null;
        this.discountedTotal = null;
        this.userBalance = 0; // User's available store balance
        this.usingStoreBalance = false;
        this.balanceApplied = 0;
        this.giftCardTotal = 0;
        this.regularSubtotal = 0;
        this.useSavedAddress = true; // Default to saved address
    }

    static getInstance(apiClient) {
        if (!OrderManager.instance) {
            OrderManager.instance = new OrderManager(apiClient);
        }
        return OrderManager.instance;
    }

    // Navigate to checkout page
    async goToCheckout() {
        if (this.cartManager.items.length === 0) {
            this.uiManager.showToast("Your cart is empty!", "error");
            return;
        }

        this.cartManager.close();
        await window.router.navigate('/checkout');
    }

    toggleAddressSelection(type) {
        const savedSection = document.getElementById('saved-address-section');
        const newSection = document.getElementById('new-address-section');
        const savedBtn = document.getElementById('use-saved-address-btn');
        const newBtn = document.getElementById('use-new-address-btn');

        if (type === 'saved') {
            this.useSavedAddress = true;
            savedSection.classList.remove('hidden');
            newSection.classList.add('hidden');

            // Update tab styles
            savedBtn.classList.add('border-blue-600', 'text-blue-600');
            savedBtn.classList.remove('text-gray-500', 'border-transparent');
            newBtn.classList.remove('border-blue-600', 'text-blue-600');
            newBtn.classList.add('text-gray-500', 'border-transparent');
        } else {
            this.useSavedAddress = false;
            savedSection.classList.add('hidden');
            newSection.classList.remove('hidden');

            // Update tab styles
            newBtn.classList.add('border-blue-600', 'text-blue-600');
            newBtn.classList.remove('text-gray-500', 'border-transparent');
            savedBtn.classList.remove('border-blue-600', 'text-blue-600');
            savedBtn.classList.add('text-gray-500', 'border-transparent');
        }
    }

    async renderCheckout() {
        const template = await this.componentStore.load('checkout');

        // Get user data
        const user = window.authManager.user || JSON.parse(localStorage.getItem('user')) || {};
        let html = template.replace('{{userEmail}}', user.email);

        // Parse HTML to manipulate
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');

        // Populate saved address if exists
        if (user.defaultAddress) {
            let address;
            try {
                address = typeof user.defaultAddress === 'string'
                    ? JSON.parse(user.defaultAddress)
                    : user.defaultAddress;
            } catch (e) {
                console.error('Error parsing address:', e);
                address = null;
            }

            if (address) {
                const streetEl = doc.getElementById('saved-address-street');
                const cityStateEl = doc.getElementById('saved-address-city-state');
                const countryEl = doc.getElementById('saved-address-country');

                if (streetEl) streetEl.textContent = address.street || '';
                if (cityStateEl) cityStateEl.textContent = `${address.city || ''}, ${address.state || ''} ${address.zipCode || ''}`;
                if (countryEl) countryEl.textContent = address.country || '';
            }
        } else {
            // Hide saved address section if no saved address
            const savedSection = doc.getElementById('saved-address-section');
            const savedBtn = doc.getElementById('use-saved-address-btn');
            const newBtn = doc.getElementById('use-new-address-btn');

            if (savedSection) savedSection.classList.add('hidden');
            if (savedBtn) {
                savedBtn.classList.add('hidden');
                // Auto-select new address
                this.useSavedAddress = false;
                if (newBtn) {
                    newBtn.classList.add('border-blue-600', 'text-blue-600');
                    newBtn.classList.remove('text-gray-500', 'border-transparent');
                }
            }
        }

        // Populate cart items preview
        const previewContainer = doc.getElementById('order-items-preview');
        previewContainer.innerHTML = this.cartManager.items.map(item => `
        <div class="flex justify-between items-center text-sm">
            <div class="flex-1">
                <span class="font-medium">${item.name}</span>
                <span class="text-xs text-gray-500 ml-2">x${item.quantity}</span>
            </div>
            <span class="font-medium">$${(item.price * item.quantity).toFixed(2)}</span>
        </div>
    `).join('');

        // Check for gift cards
        const giftCardItems = this.cartManager.items.filter(item => item.isGiftCard);
        if (giftCardItems.length > 0) {
            const giftSection = doc.getElementById('gift-cards-section');
            giftSection.classList.remove('hidden');

            const giftHtml = `
                <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                    <h2 class="text-lg font-bold text-gray-900 mb-4 flex items-center">
                        <span class="text-2xl mr-2">🎁</span> Gift Card Details
                    </h2>
                    <div class="space-y-4">
                        ${giftCardItems.map((item, index) => `
                            <div class="p-4 bg-purple-50 rounded-xl">
                                <p class="font-medium text-gray-700 mb-3">${item.name}</p>
                                <div class="space-y-3">
                                    <div>
                                        <label class="block text-xs font-medium text-gray-600 mb-1">Recipient Email</label>
                                        <input type="email" 
                                            id="gift-recipient-${index}" 
                                            class="gift-recipient w-full px-3 py-2 bg-white rounded-lg border border-gray-200 focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition text-sm"
                                            placeholder="friend@example.com"
                                            ${this.authManager.user?.email ? `value="${this.authManager.user.email}"` : ''}>
                                    </div>
                                    <div>
                                        <label class="block text-xs font-medium text-gray-600 mb-1">Personal Message (Optional)</label>
                                        <textarea 
                                            id="gift-message-${index}"
                                            rows="2"
                                            class="gift-message w-full px-3 py-2 bg-white rounded-lg border border-gray-200 focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition text-sm"
                                            placeholder="Happy Birthday!"></textarea>
                                    </div>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                    <p class="text-xs text-gray-500 mt-4">Gift cards will be emailed directly to recipients</p>
                </div>
            `;
            giftSection.innerHTML = giftHtml;
        }

        // Calculate and display totals
        this.updateCheckoutTotals(doc);

        // Fetch user balance for store balance option
        this.fetchUserBalance();

        return doc.body.innerHTML;
    }

    async processCheckout() {
        let shippingAddress;

        if (this.useSavedAddress) {
            // Get saved address from user
            const user = window.authManager.user || JSON.parse(localStorage.getItem('user')) || {};
            if (!user.defaultAddress) {
                this.uiManager.showToast('No saved address found. Please add a new address.', 'error');
                return;
            }

            try {
                shippingAddress = typeof user.defaultAddress === 'string'
                    ? JSON.parse(user.defaultAddress)
                    : user.defaultAddress;
            } catch (e) {
                this.uiManager.showToast('Error loading saved address', 'error');
                return;
            }
        } else {
            // Get address from form
            const street = document.getElementById('shipping-street')?.value;
            const city = document.getElementById('shipping-city')?.value;
            const state = document.getElementById('shipping-state')?.value;
            const zip = document.getElementById('shipping-zip')?.value;
            const country = document.getElementById('shipping-country')?.value;

            if (!street || !city || !state || !zip || !country) {
                this.uiManager.showToast('Please fill in all shipping fields', 'error');
                return;
            }

            shippingAddress = {
                street, city, state, zipCode: zip, country
            };
        }

        // Validate gift cards if present
        const giftCardItems = this.cartManager.items.filter(item => item.isGiftCard);
        if (giftCardItems.length > 0) {
            let hasValidEmails = true;

            giftCardItems.forEach((item, index) => {
                const recipientInput = document.getElementById(`gift-recipient-${index}`);
                if (recipientInput) {
                    const email = recipientInput.value.trim();
                    if (!email || !this.isValidEmail(email)) {
                        this.uiManager.showToast(`Please enter a valid email for ${item.name}`, "error");
                        hasValidEmails = false;
                        return;
                    }
                    item.recipientEmail = email;
                    item.message = document.getElementById(`gift-message-${index}`)?.value.trim() || '';
                }
            });

            if (!hasValidEmails) return;
        }

        // Show processing modal
        document.getElementById('payment-processing-modal').classList.remove('hidden');
        document.getElementById('payment-processing-modal').classList.add('flex');

        // Prepare gift card purchases
        const giftCardPurchases = this.cartManager.items
            .filter(item => item.isGiftCard)
            .map(item => ({
                amount: item.price,
                recipientEmail: item.recipientEmail,
                message: item.message || ''
            }));

        // Prepare request body
        const requestBody = {
            couponCode: this.appliedCoupon ? this.appliedCoupon.code : null,
            giftCards: giftCardPurchases,
            useStoreBalance: this.usingStoreBalance,
            storeBalanceAmount: this.balanceApplied,
            shippingAddress: JSON.stringify(shippingAddress)
        };

        try {
            // Simulate payment processing delay
            await new Promise(resolve => setTimeout(resolve, 2000));

            const response = await this.apiClient.fetch('/api/orders/place', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestBody)
            });

            // Hide processing modal
            document.getElementById('payment-processing-modal').classList.add('hidden');
            document.getElementById('payment-processing-modal').classList.remove('flex');

            // Store order data before clearing cart
            const orderData = {
                id: response.id || 'FAKE-' + Math.floor(Math.random() * 1000000),
                items: [...this.cartManager.items],
                appliedCoupon: this.appliedCoupon ? { ...this.appliedCoupon } : null,
                regularSubtotal: this.regularSubtotal,
                giftCardTotal: this.giftCardTotal,
                balanceApplied: this.balanceApplied,
                shippingAddress: shippingAddress,
                useSavedAddress: this.useSavedAddress
            };

            // Clear cart and reset state
            await this.cartManager.syncWithServer();
            this.appliedCoupon = null;
            this.usingStoreBalance = false;
            this.balanceApplied = 0;

            // Navigate to success page with data
            window.router.navigate('/order-success', orderData);

        } catch (error) {
            document.getElementById('payment-processing-modal').classList.add('hidden');
            document.getElementById('payment-processing-modal').classList.remove('flex');
            this.uiManager.showToast("Order failed: " + error.message, "error");
        }
    }

    // Helper method to validate email
    isValidEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    // Update checkout totals
    updateCheckoutTotals(doc) {
        const regularItems = this.cartManager.items.filter(item => !item.isGiftCard);
        const giftCardItems = this.cartManager.items.filter(item => item.isGiftCard);

        this.regularSubtotal = regularItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        this.giftCardTotal = giftCardItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        const subtotal = this.regularSubtotal + this.giftCardTotal;

        const subtotalEl = doc.getElementById('summary-subtotal');
        const totalEl = doc.getElementById('summary-total');

        if (subtotalEl) subtotalEl.textContent = `$${subtotal.toFixed(2)}`;
        if (totalEl) totalEl.textContent = `$${subtotal.toFixed(2)}`;

    }

    // Toggle store balance
    async toggleStoreBalance() {
        const checkbox = document.getElementById('use-store-balance');
        this.usingStoreBalance = checkbox.checked;

        if (this.usingStoreBalance) {
            // Fetch user's current balance if not already loaded
            if (this.userBalance === 0) {
                await this.fetchUserBalance();
            }

            // Show balance info
            document.getElementById('store-balance-info').classList.remove('hidden');

            // Calculate balance to apply (only to physical products)
            this.calculateBalanceApplication();
        } else {
            // Hide balance info and reset
            document.getElementById('store-balance-info').classList.add('hidden');
            this.balanceApplied = 0;
            this.updateTotalsAfterBalanceChange();
        }
    }

    // Fetch user's available store balance
    async fetchUserBalance() {
        try {
            const data = await this.apiClient.fetch('/api/gift-cards/history');
            this.userBalance = data.userStoreBalance || 0;
            document.getElementById('available-balance').textContent = `$${this.userBalance.toFixed(2)}`;
        } catch (error) {
            console.error('Error fetching user balance:', error);
            this.userBalance = 0;
        }
    }

    // Calculate how much balance to apply (only to physical products)
    calculateBalanceApplication() {
        // Get physical products total (non-gift cards)
        const physicalItems = this.cartManager.items.filter(item => !item.isGiftCard);
        const physicalTotal = physicalItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);

        // Apply balance (can't exceed physical total or available balance)
        this.balanceApplied = Math.min(physicalTotal, this.userBalance);

        // Update display
        document.getElementById('applied-balance').textContent = `$${this.balanceApplied.toFixed(2)}`;

        // Update totals
        this.updateTotalsAfterBalanceChange();
    }

    // Update totals after balance change
    updateTotalsAfterBalanceChange() {
        const regularItems = this.cartManager.items.filter(item => !item.isGiftCard);
        const giftCardItems = this.cartManager.items.filter(item => item.isGiftCard);

        this.regularSubtotal = regularItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        this.giftCardTotal = giftCardItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);

        // Calculate final totals
        const discountAmount = this.appliedCoupon?.discountAmount || 0;
        const physicalAfterDiscount = this.regularSubtotal - discountAmount;
        const physicalAfterBalance = Math.max(0, physicalAfterDiscount - this.balanceApplied);
        const finalTotal = physicalAfterBalance + this.giftCardTotal;

        // Update UI
        const subtotalEl = document.getElementById('summary-subtotal');
        const totalEl = document.getElementById('summary-total');
        const discountRow = document.getElementById('summary-discount-row');

        if (subtotalEl) subtotalEl.textContent = `$${(this.regularSubtotal + this.giftCardTotal).toFixed(2)}`;

        // Show balance row if balance applied
        let balanceRow = document.getElementById('balance-row');
        if (this.balanceApplied > 0) {
            if (!balanceRow) {
                // Create balance row if it doesn't exist
                const discountRow = document.getElementById('summary-discount-row');
                balanceRow = document.createElement('div');
                balanceRow.id = 'balance-row';
                balanceRow.className = 'flex justify-between text-sm text-blue-600';
                balanceRow.innerHTML = `
                <span>Store Balance</span>
                <span id="balance-amount">-$${this.balanceApplied.toFixed(2)}</span>
            `;
                discountRow.parentNode.insertBefore(balanceRow, discountRow.nextSibling);
            } else {
                balanceRow.classList.remove('hidden');
                document.getElementById('balance-amount').textContent = `-$${this.balanceApplied.toFixed(2)}`;
            }
        } else if (balanceRow) {
            balanceRow.classList.add('hidden');
        }

        // Show/hide discount row
        if (discountAmount > 0) {
            discountRow.classList.remove('hidden');
            document.getElementById('summary-discount').textContent = `-$${discountAmount.toFixed(2)} (${this.appliedCoupon.discountPercentage}%)`;
        } else {
            discountRow.classList.add('hidden');
        }

        if (totalEl) totalEl.textContent = `$${finalTotal.toFixed(2)}`;
    }

    // Override applyCoupon to handle balance
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

        applyBtn.disabled = true;
        applyBtn.innerText = '...';
        couponMessage.classList.add('hidden');

        try {
            const data = await this.apiClient.fetch(`/api/coupons/check?code=${encodeURIComponent(code)}`);

            // Calculate discount (only on regular items)
            const regularItems = this.cartManager.items.filter(item => !item.isGiftCard);
            this.regularSubtotal = regularItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);

            const discountAmount = (this.regularSubtotal * data.discountPercentage) / 100;
            this.giftCardTotal = this.cartManager.items
                .filter(item => item.isGiftCard)
                .reduce((sum, item) => sum + (item.price * item.quantity), 0);

            // Store applied coupon
            this.appliedCoupon = {
                code: data.code,
                discountPercentage: data.discountPercentage,
                discountAmount: discountAmount
            };

            // Update UI with coupon
            document.getElementById('summary-discount-row').classList.remove('hidden');
            document.getElementById('summary-discount').textContent = `-$${discountAmount.toFixed(2)} (${data.discountPercentage}%)`;

            // Recalculate balance after coupon
            if (this.usingStoreBalance) {
                this.calculateBalanceApplication();
            } else {
                const finalTotal = (this.regularSubtotal - discountAmount) + this.giftCardTotal;
                document.getElementById('summary-total').textContent = `$${finalTotal.toFixed(2)}`;
            }

            this.showCouponMessage(`Coupon applied! You saved $${discountAmount.toFixed(2)}`, 'success');

            couponDetails.innerHTML = `
            <div class="flex items-center justify-between">
                <span class="text-green-600 font-medium">✅ ${data.code} - ${data.discountPercentage}% off</span>
                <button onclick="window.orderManager.removeCoupon()" class="text-gray-400 hover:text-red-600 ml-2">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                    </svg>
                </button>
            </div>
        `;
            couponDetails.classList.remove('hidden');

            couponInput.value = '';

        } catch (error) {
            this.showCouponMessage(error.message || 'Invalid coupon code', 'error');
            couponDetails.classList.add('hidden');
            this.removeCoupon();
        } finally {
            applyBtn.disabled = false;
            applyBtn.innerText = 'Apply';
        }
    }

    // Override removeCoupon to handle balance
    removeCoupon() {
        this.appliedCoupon = null;

        const regularItems = this.cartManager.items.filter(item => !item.isGiftCard);
        const giftCardItems = this.cartManager.items.filter(item => item.isGiftCard);

        this.regularSubtotal = regularItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        this.giftCardTotal = giftCardItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);

        // Hide discount row
        document.getElementById('summary-discount-row').classList.add('hidden');

        // Recalculate with balance if active
        if (this.usingStoreBalance) {
            this.calculateBalanceApplication();
        } else {
            const total = this.regularSubtotal + this.giftCardTotal;
            document.getElementById('summary-total').textContent = `$${total.toFixed(2)}`;
        }

        const couponDetails = document.getElementById('coupon-details');
        couponDetails.classList.add('hidden');

        this.showCouponMessage('Coupon removed', 'success');
    }

    // Show coupon message helper
    showCouponMessage(message, type) {
        const couponMessage = document.getElementById('coupon-message');
        couponMessage.textContent = message;
        couponMessage.className = `text-xs mt-2 ${type === 'error' ? 'text-red-600' : 'text-green-600'}`;
        couponMessage.classList.remove('hidden');

        setTimeout(() => {
            couponMessage.classList.add('hidden');
        }, 3000);
    }

    // Close success modal and navigate
    closeSuccessModal() {
        document.getElementById('success-modal').classList.add('hidden');
        document.getElementById('success-modal').classList.remove('flex');
        this.router.navigate('/products');
    }

    // Show success modal
    showSuccessModal(orderData) {
        const regularItems = this.cartManager.items.filter(item => !item.isGiftCard);
        const giftCardItems = this.cartManager.items.filter(item => item.isGiftCard);

        this.regularSubtotal = regularItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        this.giftCardTotal = giftCardItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        const discountAmount = this.appliedCoupon?.discountAmount || 0;
        const finalTotal = (this.regularSubtotal - discountAmount) + this.giftCardTotal;

        let detailsHtml = `
        <div class="space-y-2">
            <p><span class="font-medium">Order ID:</span> #ORD-${orderData.id || 'FAKE-' + Math.floor(Math.random() * 1000000)}</p>
            <p><span class="font-medium">Total Paid:</span> $${finalTotal.toFixed(2)}</p>
    `;

        if (giftCardItems.length > 0) {
            detailsHtml += `
            <p class="text-purple-600">🎁 ${giftCardItems.length} Gift Card(s) purchased</p>
        `;
        }

        if (this.appliedCoupon) {
            detailsHtml += `
            <p class="text-green-600">✨ Saved $${discountAmount.toFixed(2)} with coupon ${this.appliedCoupon.code}</p>
        `;
        }

        detailsHtml += `</div>`;

        document.getElementById('order-details').innerHTML = detailsHtml;
        document.getElementById('success-message').textContent =
            giftCardItems.length > 0
                ? 'Your order has been placed! Gift cards have been sent to recipients.'
                : 'Your order has been successfully placed.';

        document.getElementById('success-modal').classList.remove('hidden');
        document.getElementById('success-modal').classList.add('flex');
    }

    // Email validation helper
    isValidEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    updatePriceDisplay(regularSubtotal, discountAmount, giftCardTotal, discountPercentage) {
        const discountRow = document.getElementById('discount-row');
        const discountAmountSpan = document.getElementById('discount-amount');
        const totalSpan = document.getElementById('total-amount');

        const finalTotal = (this.regularSubtotal - discountAmount) + giftCardTotal;

        // Show discount row
        discountRow.classList.remove('hidden');
        discountAmountSpan.textContent = `-$${discountAmount.toFixed(2)} (${discountPercentage}%)`;
        totalSpan.textContent = `$${finalTotal.toFixed(2)}`;
    }

    // Reset price display (remove discount)
    resetPriceDisplay() {
        const discountRow = document.getElementById('summary-discount-row');
        const totalSpan = document.getElementById('summary-total');
        const finalTotal = (this.regularSubtotal || 0) + (this.giftCardTotal || 0);

        discountRow.classList.add('hidden');
        totalSpan.textContent = `$${finalTotal.toFixed(2)}`;
        this.appliedCoupon = null;
    }

    async finalizeCheckout() {
        const checkoutBtn = document.getElementById('checkout-btn');

        try {
            // Check if cart contains gift cards and validate recipient emails
            const giftCardItems = this.cartManager.items.filter(item => item.isGiftCard);

            if (giftCardItems.length > 0) {
                // Collect recipient emails from the form
                let hasValidEmails = true;

                giftCardItems.forEach((item, index) => {
                    const recipientInput = document.getElementById(`gift-recipient-${index}`);
                    const messageInput = document.getElementById(`gift-message-${index}`);

                    if (recipientInput) {
                        const email = recipientInput.value.trim();
                        if (!email || !this.isValidEmail(email)) {
                            this.uiManager.showToast(`Please enter a valid email for ${item.name}`, "error", 4000);
                            hasValidEmails = false;
                            return;
                        }

                        // Update the cart item with recipient info
                        item.recipientEmail = email;
                        item.message = messageInput ? messageInput.value.trim() : '';
                    }
                });

                if (!hasValidEmails) return;
            }

            // Prepare gift card purchases for backend
            const giftCardPurchases = this.cartManager.items
                .filter(item => item.isGiftCard)
                .map(item => ({
                    amount: item.price,
                    recipientEmail: item.recipientEmail,
                    message: item.message || ''
                }));

            // Prepare request body
            const requestBody = {
                couponCode: this.appliedCoupon ? this.appliedCoupon.code : null,
                giftCards: giftCardPurchases,
                useStoreBalance: false
            };

            const response = await this.apiClient.fetch('/api/orders/place', {
                method: 'POST',
                body: JSON.stringify(requestBody),
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            // Calculate totals for success message
            const regularItems = this.cartManager.items.filter(item => !item.isGiftCard);
            const regularTotal = regularItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
            this.giftCardTotal = giftCardItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);

            const discountAmount = this.appliedCoupon ? (regularTotal * this.appliedCoupon.discountPercentage) / 100 : 0;
            const finalTotal = (regularTotal - discountAmount) + this.giftCardTotal;
            const savings = this.appliedCoupon ? discountAmount : 0;

            this.cartManager.toggle();

            // Create success message with gift card details
            let giftCardSuccessHtml = '';
            if (giftCardItems.length > 0) {
                giftCardSuccessHtml = `
                <div class="bg-purple-50 p-6 rounded-2xl mb-6">
                    <h3 class="font-bold text-purple-800 mb-3 flex items-center">
                        <span class="text-2xl mr-2">🎁</span> Gift Cards Sent!
                    </h3>
                    ${giftCardItems.map(item => `
                        <div class="text-left mb-3 pb-3 border-b border-purple-200 last:border-0">
                            <p class="font-medium">${item.name}</p>
                            <p class="text-sm text-purple-600">Sent to: ${item.recipientEmail}</p>
                            ${item.message ? `<p class="text-xs text-gray-500 mt-1">"${item.message}"</p>` : ''}
                        </div>
                    `).join('')}
                    <p class="text-sm text-purple-600 mt-2">✨ Gift card codes have been emailed to recipients!</p>
                </div>
            `;
            }

            document.getElementById('content').innerHTML = `
        <div class="text-center py-20 animate-fade-in">
            <div class="bg-green-100 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
                <svg class="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path></svg>
            </div>
            <h2 class="text-4xl font-black text-gray-900 mb-4">Success!</h2>
            <p class="text-gray-600 text-lg mb-4">Your order has been received. Transaction ID: #FAKE-${Math.floor(Math.random() * 100000000)}</p>
            
            ${giftCardSuccessHtml}
            
            ${this.appliedCoupon ? `
            <div class="bg-green-50 p-4 rounded-2xl mb-6 inline-block">
                <p class="text-green-700 font-medium">🎉 You saved $${savings.toFixed(2)} on products with coupon ${this.appliedCoupon.code}!</p>
            </div>
            ` : ''}
            
            <div class="bg-gray-50 p-4 rounded-2xl mb-8 inline-block">
                <p class="text-gray-700">
                    ${regularTotal > 0 ? `Products: $${regularTotal.toFixed(2)}` : ''}
                    ${this.giftCardTotal > 0 ? (regularTotal > 0 ? ' + ' : '') + `Gift Cards: $${giftCardTotal.toFixed(2)}` : ''}
                    ${this.appliedCoupon ? `<br><span class="text-green-600">- Discount: $${discountAmount.toFixed(2)}</span>` : ''}
                    <br><span class="font-black">Total paid: <span class="text-blue-600">$${finalTotal.toFixed(2)}</span></span>
                </p>
            </div>
            
            <button onclick="window.router.navigate('/products')" class="bg-blue-600 text-white px-8 py-3 rounded-xl font-bold hover:bg-black transition">
                Back to Shop
            </button>
        </div>
    `;

            this.closePaymentModal();
            await this.cartManager.syncWithServer();

            // Reset coupon and gift card state
            this.appliedCoupon = null;
            this.discountedTotal = null;
            this.regularSubtotal = 0;
            this.giftCardTotal = 0;

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

    // Helper method to validate email
    isValidEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    // Render order success page
    async renderOrderSuccess(orderData) {
        const template = await this.componentStore.load('order-success');

        // Parse date
        const now = new Date();
        const orderDate = now.toLocaleDateString() + ' at ' + now.toLocaleTimeString();

        let html = template.replace('{{orderDate}}', orderDate);

        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');

        // Set order ID
        doc.getElementById('success-order-id').textContent = '#ORD-' + (orderData.id || Math.floor(Math.random() * 1000000));

        // Populate order items
        const itemsContainer = doc.getElementById('success-order-items');
        itemsContainer.innerHTML = orderData.items.map(item => `
        <div class="flex justify-between items-center">
            <div class="flex items-center">
                <div class="w-12 h-12 bg-gray-100 rounded-lg overflow-hidden mr-3">
                    <img src="${item.imageUrl || `https://placehold.co/600x400/9333ea/ffffff?text=Gift+Card+${item.price}`}" 
                         alt="${item.name}" 
                         class="w-full h-full object-cover">
                </div>
                <div>
                    <p class="font-medium text-gray-900">${item.name}</p>
                    <p class="text-xs text-gray-500">Qty: ${item.quantity}</p>
                </div>
            </div>
            <span class="font-medium">$${(item.price * item.quantity).toFixed(2)}</span>
        </div>
    `).join('');

        // Calculate totals
        const regularItems = orderData.items.filter(item => !item.isGiftCard);
        const giftCardItems = orderData.items.filter(item => item.isGiftCard);

        const regularSubtotal = regularItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        const giftCardTotal = giftCardItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        const discountAmount = orderData.appliedCoupon?.discountAmount || 0;
        const finalTotal = (regularSubtotal - discountAmount) + giftCardTotal;

        // Set totals
        doc.getElementById('success-subtotal').textContent = `$${(regularSubtotal + giftCardTotal).toFixed(2)}`;
        doc.getElementById('success-total').textContent = `$${finalTotal.toFixed(2)}`;

        if (orderData.balanceApplied && orderData.balanceApplied > 0) {
            const balanceRow = doc.getElementById('success-balance-row');
            balanceRow.classList.remove('hidden');
            doc.getElementById('success-balance').textContent = `-$${orderData.balanceApplied.toFixed(2)}`;
        }

        // Show discount if applied
        if (orderData.appliedCoupon) {
            doc.getElementById('success-discount').textContent = `-$${discountAmount.toFixed(2)} (${orderData.appliedCoupon.discountPercentage}%)`;
        } else {
            doc.getElementById('success-discount-row').classList.add('hidden');
        }

        // Show gift cards section if present
        if (giftCardItems.length > 0) {
            const giftSection = doc.getElementById('success-gift-cards');
            giftSection.classList.remove('hidden');

            giftSection.innerHTML = `
            <h3 class="text-lg font-bold text-gray-900 mb-4 flex items-center">
                <span class="text-2xl mr-2">🎁</span> Gift Cards Purchased
            </h3>
            <div class="space-y-3">
                ${giftCardItems.map(item => `
                    <div class="bg-white p-4 rounded-xl">
                        <p class="font-medium text-gray-900">${item.name}</p>
                        <p class="text-sm text-purple-600">Sent to: ${item.recipientEmail || 'Pending'}</p>
                        ${item.message ? `<p class="text-xs text-gray-500 mt-1">"${item.message}"</p>` : ''}
                    </div>
                `).join('')}
            </div>
            <p class="text-sm text-purple-600 mt-4">✨ Gift card codes have been emailed to recipients!</p>
        `;
        }

        // Set shipping address from order data
        if (orderData.shippingAddress) {
            const addr = orderData.shippingAddress;
            doc.getElementById('success-shipping-address').textContent =
                `${addr.street}, ${addr.city}, ${addr.state} ${addr.zipCode}, ${addr.country}`;
        }

        return doc.body.innerHTML;
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