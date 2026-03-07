import { ComponentStore } from '../core/ComponentStore.js';
import { UIManager } from './UIManager.js';
import { Utils } from '../core/Utils.js';
import { Constants } from '../config/Constants.js';

export class AdminManager {
    static instance = null;

    constructor(apiClient) {
        this.apiClient = apiClient;
        this.componentStore = ComponentStore.getInstance();
        this.uiManager = UIManager.getInstance();
        this.router = null;
        this.allOrdersCache = [];
        this.statusColors = Constants.STATUS_COLORS;
        this.allOrdersCache = [];
    }

    static getInstance(apiClient) {
        if (!AdminManager.instance) {
            AdminManager.instance = new AdminManager(apiClient);
        }
        return AdminManager.instance;
    }

    async setRouter(router) {
        this.router = router;
    }

    async getStats() {
        try {
            return await this.apiClient.fetch('/api/admin/stats');
        } catch (error) {
            this.uiManager.showToast('Error fetching stats: ' + error.message, 'error');
            return null;
        }
    }

    async getAllOrders() {
        try {
            this.allOrdersCache = await this.apiClient.fetch('/api/admin/orders') || [];
            return this.allOrdersCache;
        } catch (error) {
            this.uiManager.showToast('Error fetching orders: ' + error.message, 'error');
            return [];
        }
    }

    async getAllUsers() {
        try {
            return await this.apiClient.fetch('/api/admin/users') || [];
        } catch (error) {
            this.uiManager.showToast('Error fetching users: ' + error.message, 'error');
            return [];
        }
    }

    async updateOrderStatus(orderId, newStatus) {
        try {
            await this.apiClient.fetch(`/api/admin/orders/${orderId}/status?status=${newStatus}`, {
                method: 'PATCH'
            });

            const badge = document.getElementById(`status-badge-${orderId}`);
            if (badge) {
                badge.innerText = newStatus;
                badge.className = `inline-block w-fit px-3 py-1 rounded-full text-[10px] font-bold uppercase ${this.statusColors[newStatus]}`;
                this.uiManager.showToast(`Order #${orderId} status updated to ${newStatus}`);
            }
        } catch (error) {
            this.uiManager.showToast('Error updating order status: ' + error.message, 'error');
        }
    }

    async updateUserRole(userId, newRole) {
        try {
            await this.apiClient.fetch(`/api/admin/users/${userId}/role?role=${newRole}`, {
                method: 'PATCH'
            });
            this.uiManager.showToast('Permissions updated!');
            this.switchTab('users'); // Refresh
        } catch (error) {
            this.uiManager.showToast('Self-downgrade protected.', 'error');
        }
    }

    async deleteProduct(productId) {
        if (!await this.uiManager.confirm('Delete Product', 'Are you sure? This will permanently remove this product.')) return;

        try {
            await this.apiClient.fetch(`/api/admin/products/${productId}`, {
                method: 'DELETE'
            });
            document.getElementById(`delete-product-${productId}`)?.remove();
            this.uiManager.showToast('Product deleted successfully!');
        } catch (error) {
            this.uiManager.showToast('Error deleting product: ' + error.message, 'error');
        }
    }

    async saveProduct(event) {
        event.preventDefault();
        const form = event.target;
        const formData = new FormData();

        const productData = {
            name: form.name.value,
            price: form.price.value,
            stock: form.stock.value,
            description: form.description.value,
            categoryName: form.categoryName.value
        };

        formData.append("product", new Blob([JSON.stringify(productData)], {
            type: "application/json"
        }));

        const fileInput = document.getElementById('product-image-input');
        if (fileInput.files[0]) {
            formData.append("file", fileInput.files[0]);
        }

        const res = await fetch('/api/admin/products', {
            method: 'POST',
            body: formData
        });

        if (res.ok) {
            const product = await res.json();
            console.log("Saved everything in one go:", product);
            await this.router.navigate("/admin");
        }
    }

    async updateProduct(event, id) {
        event.preventDefault();
        const formData = new FormData(event.target);
        const data = Object.fromEntries(formData.entries());

        try {
            const res = await this.apiClient.fetch(`/api/admin/products/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            this.uiManager.showToast("Product updated successfully!", "success", 2000);
            await this.router.navigate("/admin");

        } catch (err) {
            console.error("Update Error:", err);
        }
    }

    async editProduct(id) {
        window.router.navigate("/admin/edit-product/" + id);        
    }

    filterOrders() {
        const searchTerm = document.getElementById('admin-order-search').value.toLowerCase();
        const statusFilter = document.getElementById('admin-order-filter').value;

        const filteredOrders = this.allOrdersCache.filter(order => {
            const matchesSearch = order.user.email.toLowerCase().includes(searchTerm) ||
                `#ORD-${order.id}`.toLowerCase().includes(searchTerm);
            const matchesStatus = statusFilter === 'ALL' || order.status === statusFilter;

            return matchesSearch && matchesStatus;
        });

        this.uiManager.showLoading('admin-orders-table');

        this.renderOrders(filteredOrders);
    }

    renderOrders(ordersToRender) {
        const tableBody = document.getElementById('admin-orders-table');

        if (ordersToRender.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="5" class="py-10 text-center text-gray-400 italic">No orders match your filters</td></tr>`;
            return;
        }

        tableBody.innerHTML = ordersToRender.map(order => {
            const itemsList = order.items.map(i => `${i.productName} (x${i.quantity})`).join(', ');
            const status = order.status || '';

            return `
                <tr class="border-b border-gray-50 hover:bg-gray-50 transition">
                    <td class="py-4 px-6 font-mono text-xs">#ORD-${order.id}</td>
                    <td class="py-4 px-2">
                        <div class="text-sm font-medium text-gray-900">${order.user.email}</div>
                        <div class="text-[10px] text-gray-400">${new Date(order.orderDate).toLocaleDateString()}</div>
                    </td>
                    <td class="py-4 px-2">
                        <div class="flex flex-col gap-2">
                            <span id="status-badge-${order.id}" class="inline-block w-fit px-3 py-1 rounded-full text-[10px] font-bold uppercase ${this.statusColors[status]}">
                                ${status || 'PENDING'}
                            </span>
                            <select onchange="window.adminManager.updateOrderStatus(${order.id}, this.value)" 
                                    class="text-[10px] border-none bg-gray-50 rounded-lg p-1 focus:ring-2 focus:ring-blue-500">
                                <option value="PENDING" ${order.status === 'PENDING' ? 'selected' : ''}>Pending</option>
                                <option value="SHIPPED" ${order.status === 'SHIPPED' ? 'selected' : ''}>Shipped</option>
                                <option value="DELIVERED" ${order.status === 'DELIVERED' ? 'selected' : ''}>Delivered</option>
                                <option value="CANCELLED" ${order.status === 'CANCELLED' ? 'selected' : ''}>Cancel</option>
                            </select>
                        </div>
                    </td>
                    <td class="py-4 px-2 font-bold text-gray-900">$${order.totalAmount.toFixed(2)}</td>
                    <td class="py-4 px-2 text-xs text-gray-500 max-w-xs truncate" title="${itemsList}">
                        ${itemsList}
                    </td>
                </tr>
            `;
        }).join('');
    }

    renderUsers(users) {
        const tableBody = document.getElementById('admin-users-table');
        tableBody.innerHTML = users.map(u => `
            <tr class="border-b border-gray-50 hover:bg-gray-50">
                <td class="py-4 px-6 font-mono text-xs text-gray-400">#USR-${u.id}</td>
                <td class="py-4 px-2 text-sm font-medium">${u.email}</td>
                <td class="py-4 px-2">
                    <span class="px-2 py-1 rounded-md text-[10px] font-bold ${u.role === 'ROLE_ADMIN' ? 'bg-purple-100 text-purple-700' : 'bg-gray-100 text-gray-600'}">
                        ${u.role}
                    </span>
                </td>
                <td class="py-4 px-2">
                    <select onchange="window.adminManager.updateUserRole(${u.id}, this.value)" class="text-xs bg-gray-50 border-none rounded-lg p-1">
                        <option value="ROLE_USER" ${u.role === 'ROLE_USER' ? 'selected' : ''}>User</option>
                        <option value="ROLE_ADMIN" ${u.role === 'ROLE_ADMIN' ? 'selected' : ''}>Admin</option>
                    </select>
                </td>
            </tr>
        `).join('');
    }

    async navigateToAddProduct() {
        this.router.navigate("/admin/add-product");
    }

    // Load all coupons
    async loadCoupons() {
        try {
            const coupons = await this.apiClient.fetch('/api/admin/coupons');
            this.renderCoupons(coupons);
            await this.loadCouponStats();
        } catch (error) {
            console.error('Error loading coupons:', error);
            document.getElementById('admin-coupons-table').innerHTML = `
            <tr>
                <td colspan="6" class="py-10 text-center text-red-500">
                    Failed to load coupons. Please try again.
                </td>
            </tr>
        `;
        }
    }

    // Render coupons table
    renderCoupons(coupons) {
        const tbody = document.getElementById('admin-coupons-table');

        if (!coupons || coupons.length === 0) {
            tbody.innerHTML = `
            <tr>
                <td colspan="6" class="py-10 text-center text-gray-400">
                    No coupons found. Create your first coupon!
                </td>
            </tr>
        `;
            return;
        }

        // Update active coupons count
        const activeCount = coupons.filter(c => c.active).length;
        document.getElementById('active-coupons-count').textContent = activeCount;

        tbody.innerHTML = coupons.map(coupon => {
            const isExpired = new Date(coupon.expiryDate) < new Date();
            const expiryDate = new Date(coupon.expiryDate).toLocaleDateString();
            const usagePercentage = (coupon.timesUsed / coupon.usageLimit * 100).toFixed(1);

            return `
            <tr class="border-b border-gray-50 hover:bg-gray-50 transition">
                <td class="py-4 px-6">
                    <span class="font-mono font-bold text-purple-600">${coupon.code}</span>
                </td>
                <td class="py-4 px-2">
                    <span class="font-bold text-gray-900">${coupon.discountPercentage}%</span>
                </td>
                <td class="py-4 px-2">
                    <span class="${isExpired ? 'text-red-500' : 'text-gray-600'}">
                        ${expiryDate}
                        ${isExpired ? ' (Expired)' : ''}
                    </span>
                </td>
                <td class="py-4 px-2">
                    <div class="flex flex-col">
                        <span class="text-sm font-medium">${coupon.timesUsed}/${coupon.usageLimit}</span>
                        <div class="w-24 h-1.5 bg-gray-200 rounded-full mt-1">
                            <div class="h-1.5 bg-purple-600 rounded-full" style="width: ${usagePercentage}%"></div>
                        </div>
                    </div>
                </td>
                <td class="py-4 px-2">
                    <span class="px-3 py-1 rounded-full text-xs font-bold uppercase 
                        ${coupon.active && !isExpired ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}">
                        ${coupon.active && !isExpired ? 'Active' : 'Inactive'}
                    </span>
                </td>
                <td class="py-4 px-2">
                    <div class="flex space-x-2">
                        <button onclick="window.adminManager.editCoupon(${coupon.id})"
                            class="text-gray-400 hover:text-purple-600 transition p-1">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path>
                            </svg>
                        </button>
                        <button onclick="window.adminManager.promptDeleteCoupon(${coupon.id}, '${coupon.code}')"
                            class="text-gray-400 hover:text-red-600 transition p-1">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                            </svg>
                        </button>
                    </div>
                </td>
            </tr>
        `;
        }).join('');
    }

    // Load top coupon stats
    async loadCouponStats() {
        try {
            const topCoupons = await this.apiClient.fetch('/api/admin/coupons/stats');
            const statsContainer = document.getElementById('top-coupons-stats');

            if (!topCoupons || topCoupons.length === 0) {
                statsContainer.innerHTML = `
                <div class="col-span-5 text-center text-gray-400 text-sm py-4">
                    No coupon usage data yet
                </div>
            `;
                return;
            }

            statsContainer.innerHTML = topCoupons.map(coupon => `
            <div class="bg-white p-3 rounded-xl border border-gray-100">
                <div class="text-xs text-gray-400 mb-1">${coupon.code}</div>
                <div class="font-bold text-purple-600">${coupon.timesUsed} uses</div>
                <div class="text-xs text-gray-500">${coupon.discountPercentage}% off</div>
            </div>
        `).join('');

        } catch (error) {
            console.error('Error loading coupon stats:', error);
        }
    }

    // Show coupon modal (for create/edit)
    showCouponModal(couponData = null) {
        const modal = document.getElementById('coupon-modal');
        const title = document.getElementById('coupon-modal-title');
        const form = document.getElementById('coupon-form');

        if (couponData) {
            // Edit mode
            title.textContent = 'Edit Coupon';
            document.getElementById('coupon-id').value = couponData.id || '';
            document.getElementById('coupon-code').value = couponData.code || '';
            document.getElementById('coupon-discount').value = couponData.discountPercentage || '';

            // Format date for input
            if (couponData.expiryDate) {
                const date = new Date(couponData.expiryDate);
                const formattedDate = date.toISOString().split('T')[0];
                document.getElementById('coupon-expiry').value = formattedDate;
            }

            document.getElementById('coupon-limit').value = couponData.usageLimit || '';
            document.getElementById('coupon-active').checked = couponData.active || false;
        } else {
            // Create mode
            title.textContent = 'Create New Coupon';
            form.reset();
            document.getElementById('coupon-id').value = '';
            // Set default expiry date to 30 days from now
            const defaultDate = new Date();
            defaultDate.setDate(defaultDate.getDate() + 30);
            document.getElementById('coupon-expiry').value = defaultDate.toISOString().split('T')[0];
            document.getElementById('coupon-active').checked = true;
        }

        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }

    // Hide coupon modal
    hideCouponModal() {
        const modal = document.getElementById('coupon-modal');
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }

    // Save coupon (create or update)
    async saveCoupon(event) {
        event.preventDefault();

        const formData = new FormData(event.target);
        const couponData = {
            code: formData.get('code'),
            discountPercentage: parseInt(formData.get('discountPercentage')),
            expiryDate: formData.get('expiryDate'),
            usageLimit: parseInt(formData.get('usageLimit')),
            active: formData.get('active') === 'on'
        };

        const couponId = formData.get('id');

        try {
            let response;
            if (couponId) {
                // Update existing coupon
                response = await this.apiClient.fetch(`/api/admin/coupons/${couponId}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(couponData)
                });
                this.uiManager.showToast('Coupon updated successfully!');
            } else {
                // Create new coupon
                response = await this.apiClient.fetch('/api/admin/coupons', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(couponData)
                });
                this.uiManager.showToast('Coupon created successfully!');
            }

            this.hideCouponModal();
            await this.loadCoupons(); // Refresh the list

        } catch (error) {
            this.uiManager.showToast('Error saving coupon: ' + error.message, 'error');
        }
    }

    // Edit coupon
    async editCoupon(id) {
        try {
            const coupons = await this.apiClient.fetch('/api/admin/coupons');
            const coupon = coupons.find(c => c.id === id);
            if (coupon) {
                this.showCouponModal(coupon);
            }
        } catch (error) {
            this.uiManager.showToast('Error loading coupon details', 'error');
        }
    }

    // Prompt delete coupon
    promptDeleteCoupon(id, code) {
        this.deleteCouponId = id;
        document.getElementById('delete-coupon-message').textContent =
            `Are you sure you want to delete coupon "${code}"? This action cannot be undone.`;
        document.getElementById('delete-coupon-modal').classList.remove('hidden');
        document.getElementById('delete-coupon-modal').classList.add('flex');
    }

    // Hide delete modal
    hideDeleteModal() {
        document.getElementById('delete-coupon-modal').classList.add('hidden');
        document.getElementById('delete-coupon-modal').classList.remove('flex');
        this.deleteCouponId = null;
    }

    // Confirm delete coupon
    async confirmDeleteCoupon() {
        if (!this.deleteCouponId) return;

        try {
            await this.apiClient.fetch(`/api/admin/coupons/${this.deleteCouponId}`, {
                method: 'DELETE'
            });

            this.uiManager.showToast('Coupon deleted successfully!');
            this.hideDeleteModal();
            await this.loadCoupons(); // Refresh the list

        } catch (error) {
            this.uiManager.showToast('Error deleting coupon: ' + error.message, 'error');
        }
    }

    async getApiRoutes() {
        try {
            return await this.apiClient.fetch('/api/admin/routes');
        } catch (error) {
            this.uiManager.showToast('Error fetching API routes: ' + error.message, 'error');
            return [];
        }
    }

    async switchTab(tab) {
        const sections = ['inventory', 'coupons', 'orders', 'users'];
        sections.forEach(s => {
            const section = document.getElementById(`section-${s}`);
            if (section) {
                section.classList.toggle('hidden', s !== tab);
            }

            const btn = document.getElementById(`tab-${s}`);
            if (btn) {
                if (s === tab) {
                    btn.className = 'pb-4 px-2 border-b-2 border-blue-600 font-bold text-blue-600';
                } else {
                    btn.className = 'pb-4 px-2 border-b-2 border-transparent text-gray-400 hover:text-gray-600 font-bold';
                }
            }
        });

        // Load data based on tab
        if (tab === 'coupons') {
            await this.loadCoupons();
        } else if (tab === 'orders') {
            const orders = await this.getAllOrders();
            this.renderOrders(orders);
        } else if (tab === 'users') {
            const users = await this.getAllUsers();
            this.renderUsers(users);
        }
    }
}