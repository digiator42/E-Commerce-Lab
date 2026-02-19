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
        if (!confirm('Are you sure? This will permanently remove this product.')) return;

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

    async saveProduct(formData) {
        try {
            const res = await fetch('/api/admin/products', {
                method: 'POST',
                body: formData
            });

            if (res.ok) {
                const product = await res.json();
                this.uiManager.showToast('Product created successfully!');
                window.history.pushState({}, '', '/admin');
                window.router.route();
            }
        } catch (error) {
            this.uiManager.showToast('Error saving product: ' + error.message, 'error');
        }
    }

    async updateProduct(productId, data) {
        try {
            await this.apiClient.fetch(`/api/admin/products/${productId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            this.uiManager.showToast('Product updated successfully!');
            window.location.href = '/admin';
        } catch (error) {
            this.uiManager.showToast('Error updating product: ' + error.message, 'error');
        }
    }

    filterOrders(searchTerm, statusFilter) {
        const filteredOrders = this.allOrdersCache.filter(order => {
            const matchesSearch = order.user.email.toLowerCase().includes(searchTerm) ||
                `#ORD-${order.id}`.toLowerCase().includes(searchTerm);
            const matchesStatus = statusFilter === 'ALL' || order.status === statusFilter;
            return matchesSearch && matchesStatus;
        });
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

    async switchTab(tab) {
        const sections = ['inventory', 'orders', 'users'];
        sections.forEach(s => {
            document.getElementById(`section-${s}`).classList.toggle('hidden', s !== tab);
            const btn = document.getElementById(`tab-${s}`);
            if (s === tab) {
                btn.className = 'pb-4 px-2 border-b-2 border-blue-600 font-bold text-blue-600';
            } else {
                btn.className = 'pb-4 px-2 text-gray-400 font-bold';
            }
        });

        if (tab === 'orders') {
            const orders = await this.getAllOrders();
            this.renderOrders(orders);
        }

        if (tab === 'users') {
            const users = await this.getAllUsers();
            this.renderUsers(users);
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

    async navigateToAddProduct() {
        this.router.navigate("/admin/add-product");
    }
}