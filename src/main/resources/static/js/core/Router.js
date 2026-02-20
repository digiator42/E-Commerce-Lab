import { ComponentStore } from './ComponentStore.js';
import { UIManager } from '../modules/UIManager.js';
import { Constants } from '../config/Constants.js';

export class Router {
    static instance = null;

    constructor() {
        this.componentStore = ComponentStore.getInstance();
        this.uiManager = UIManager.getInstance();
        this.cartManager = null;
        this.authManager = null;
        this.productManager = null;
        this.orderManager = null;
        this.adminManager = null;
        this.apiClient = null;
        this.routes = null;
        this.routes = null;
    }

    static getInstance() {
        if (!Router.instance) {
            Router.instance = new Router();
        }
        return Router.instance;
    }

    setAuthManager(authManager) {
        this.authManager = authManager;
        this.routes = this.initRoutes();
    }

    setProductManager(productManager) {
        this.productManager = productManager;
    }

    setOrderManager(orderManager) {
        this.orderManager = orderManager;
    }

    setAdminManager(adminManager) {
        this.adminManager = adminManager;
    }

    setCartManager(cartManager) {
        this.cartManager = cartManager;
    }

    setApiClient(apiClient) {
        this.apiClient = apiClient;
    }

    initRoutes() {

        if (!this.authManager) {
            console.error('AuthManager not set in Router');
            return {};
        }

        return {
            '/': async () => {
                return await this.componentStore.load('home');
            },

            '/admin': async () => {
                const template = await this.componentStore.load('admin-dashboard');
                const [statsRes, productsRes] = await Promise.all([
                    this.adminManager.getStats(),
                    this.apiClient.fetch('/api/products?size=100')
                ]);

                const productRows = productsRes.content.map(p => `
                    <tr id="delete-product-${p.id}" class="border-b border-gray-50 hover:bg-gray-50 transition">
                        <td class="py-4 px-2 font-medium text-gray-900">${p.name}</td>
                        <td class="py-4 px-2 text-gray-500">${p.category}</td>
                        <td class="py-4 px-2 font-bold text-blue-600">$${p.price.toFixed(2)}</td>
                        <td class="py-4 px-2">
                            <div class="flex space-x-2">
                                <button onclick="window.router.navigate('/admin/edit-product/${p.id}')" class="text-gray-400 hover:text-blue-600">
                                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path>
                                    </svg>
                                </button>
                                <button onclick="window.adminManager.deleteProduct(${p.id})" class="text-gray-400 hover:text-red-600">
                                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                                    </svg>
                                </button>
                            </div>
                        </td>
                    </tr>
                `).join('');

                return template
                    .replace('{{totalRevenue}}', statsRes?.revenue?.toFixed(2) || '0.00')
                    .replace('{{totalOrders}}', statsRes?.orders || '0')
                    .replace('{{totalProducts}}', statsRes?.products || '0')
                    .replace('{{productRows}}', productRows);
            },

            '/admin/add-product': async () => {
                const [template, categories] = await Promise.all([
                    this.componentStore.load('add-product'),
                    this.apiClient.fetch('/api/categories')
                ]);

                const categoryOptions = categories.map(cat =>
                    `<option value="${cat.name}">${cat.name}</option>`
                ).join('');

                return template.replace('{{categoryOptions}}', categoryOptions);
            },

            '/admin/routes': async () => {
                const template = `
                    <div class="p-6">
                        <h2 class="text-2xl font-bold mb-6">Backend API Explorer</h2>
                        <div class="overflow-hidden rounded-xl border border-gray-200 bg-white">
                            <table class="min-w-full divide-y divide-gray-200">
                                <thead class="bg-gray-50">
                                    <tr>
                                        <th class="px-6 py-3 text-left text-xs font-bold text-gray-500 uppercase">Path</th>
                                        <th class="px-6 py-3 text-left text-xs font-bold text-gray-500 uppercase">Methods</th>
                                        <th class="px-6 py-3 text-left text-xs font-bold text-gray-500 uppercase">Status</th>
                                        <th class="px-6 py-3 text-left text-xs font-bold text-gray-500 uppercase">Handler</th>
                                    </tr>
                                </thead>
                                <tbody id="api-route-rows" class="divide-y divide-gray-200">
                                    <tr><td colspan="4" class="p-4 text-center">Loading API routes...</td></tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                `;

                setTimeout(async () => {
                    const data = await this.adminManager.getApiRoutes();
                    const rowsHtml = data.map(route => `
                        <tr class="hover:bg-gray-50">
                            <td class="px-6 py-4 font-mono text-sm text-blue-600">
                                ${Array.isArray(route.path) ? route.path.join(', ') : route.path}
                            </td>
                            <td class="px-6 py-4">
                                ${route.methods.map(m => `
                                    <span class="px-2 py-1 text-xs font-bold rounded ${Utils.getMethodColor(m)}">${m}</span>
                                `).join('')}
                            </td>
                            <td class="px-6 py-4 text-sm text-gray-600">${route.expectedStatus}</td>
                            <td class="px-6 py-4 text-xs text-gray-400 italic">${route.handler}</td>
                        </tr>
                    `).join('');
                    document.getElementById('api-route-rows').innerHTML = rowsHtml;
                }, 0);

                return template;
            },

            '/products': async () => {
                const categoryParams = this.productManager.currentCategory ? `&category=${encodeURIComponent(this.productManager.currentCategory)}` : '';
                const searchParams = encodeURIComponent(this.productManager.currentSearch);
                const productsUrl = `/api/products?page=${this.productManager.currentPage}&size=6&search=${searchParams}${categoryParams}`;

                const [template, cardTemplate, productRes, categoryRes] = await Promise.all([
                    this.componentStore.load('products'),
                    this.componentStore.load('product-card'),
                    this.apiClient.fetch(productsUrl),
                    this.apiClient.fetch('/api/categories')
                ]);

                const productListHtml = productRes?.content.map(p => {
                    const imageSrc = p.imageUrl || 'https://placehold.co/600x400/EEE/31343C';
                    return cardTemplate
                        .replace(/{{name}}/g, p.name)
                        .replace(/{{description}}/g, p.description)
                        .replace(/{{price}}/g, (p?.price ?? 0).toFixed(2))
                        .replace(/{{id}}/g, p.id)
                        .replace(/{{category}}/g, p.category)
                        .replace(/{{imageSrc}}/g, imageSrc);
                }).join('');

                const categoryHtml = `
                    <button onclick="window.productManager.filterCategory(null)" 
                        class="w-full text-left px-4 py-2 rounded-xl transition ${!this.productManager.currentCategory ? 'bg-blue-50 text-blue-600 font-bold' : 'text-gray-500 hover:bg-gray-50'}">
                        All Products
                    </button>
                ` + categoryRes.map(cat => `
                    <button onclick="window.productManager.filterCategory('${cat.name}')" 
                        class="w-full text-left px-4 py-2 rounded-xl transition ${this.productManager.currentCategory === cat.name ? 'bg-blue-50 text-blue-600 font-bold' : 'text-gray-500 hover:bg-gray-50'}">
                        ${cat.icon} ${cat.name}
                    </button>
                `).join('');

                let finalHtml = template.replace('{{productList}}', productListHtml);
                const parser = new DOMParser();
                const doc = parser.parseFromString(finalHtml, 'text/html');
                const catContainer = doc.getElementById('category-list');
                if (catContainer) catContainer.innerHTML = categoryHtml;

                this.productManager.totalPages = productRes.totalPages;
                doc.getElementById('prev-btn').disabled = productRes.first;
                doc.getElementById('next-btn').disabled = productRes.last;
                doc.getElementById('page-info').innerText = `Page ${productRes.number + 1} of ${productRes.totalPages}`;

                return doc.body.innerHTML;
            },

            '/login': async () => {
                return await this.componentStore.load('login');
            },

            '/orders': async () => {
                return await this.orderManager.renderOrders();
            },

            '/product/:id': async (params) => {
                const template = await this.componentStore.load('product-detail');
                const p = await this.apiClient.fetch(`/api/products/${params.id}`);

                const reviewsHtml = p.reviews.length > 0 ? p.reviews.map(rev => `
                    <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
                        <div class="flex items-center justify-between mb-2">
                            <span class="font-bold text-sm text-gray-900">${rev.userEmail}</span>
                            <span class="text-yellow-400 font-bold">${'★'.repeat(rev.rating)}</span>
                        </div>
                        <p class="text-gray-600 text-sm">${rev.comment}</p>
                        <p class="text-[10px] text-gray-400 mt-2">${new Date(rev.date).toLocaleString()}</p>
                    </div>
                `).join('') : '<p class="text-gray-400 italic">No reviews yet. Be the first!</p>';

                const imageSrc = p.imageUrl || 'https://placehold.co/600x400/EEE/31343C';

                return template
                    .replace(/{{name}}/g, p.name)
                    .replace(/{{stock}}/g, p.stock > 0 ? '<span class="text-green-600 font-bold">' + p.stock + ' in stock</span>' : '<span class="text-red-600 font-bold">Out of stock</span>')
                    .replace(/{{description}}/g, p.description)
                    .replace(/{{price}}/g, p.price.toFixed(2))
                    .replace(/{{category}}/g, p.category)
                    .replace(/{{imageSrc}}/g, imageSrc)
                    .replace(/{{id}}/g, p.id)
                    .replace('{{reviewsHtml}}', reviewsHtml);
            },

            '/admin/edit-product/:id': async (params) => {
                const [template, product, categories] = await Promise.all([
                    this.componentStore.load('add-product'),
                    this.apiClient.fetch(`/api/products/${params.id}`),
                    this.apiClient.fetch('/api/categories')
                ]);

                const options = categories.map(c => `
                    <option value="${c.name}" ${c.name === product.category ? 'selected' : ''}>
                        ${c.name}
                    </option>
                `).join('');

                return template
                    .replace('Create New Product', `Edit Product: ${product.name}`)
                    .replace('{{categoryOptions}}', options)
                    .replace('name="name"', `name="name" value="${product.name}"`)
                    .replace('name="price"', `name="price" value="${product.price}"`)
                    .replace('name="description"', 'name="description"')
                    .replace('name="stock"', `name="stock" value="${product.stock}"`)
                    .replace('</textarea>', `${product.description}</textarea>`)
                    .replace('onsubmit="saveProduct(event)"', `onsubmit="window.adminManager.updateProduct(${product.id}, Object.fromEntries(new FormData(event.target)))"`);
            },

            '/error': async () => {
                return await this.componentStore.load('404');
            }
        };
    }

    async navigate(path) {
        window.history.pushState(null, '', path);
        await this.route();
    }

    async route(event) {
        if (event) {
            event.preventDefault();
            const href = event.target.closest('a').getAttribute('href');
            window.history.pushState(null, '', href);
        }

        this.uiManager.toggleAuthButtons(this.authManager.isAuthenticated);

        let path = window.location.pathname;
        if (path.endsWith('/') && path.length > 1) {
            path = path.slice(0, -1);
        }

        // Admin access check
        if (path.startsWith('/admin') && !this.authManager.isAdmin()) {
            console.error('User is not an admin. Diverting...');
            window.history.pushState(null, '', '/orders');
            return await this.route();
        }

        // Authentication check
        if (!this.authManager.isAuthenticated && !Constants.PUBLIC_PATHS.includes(path)) {
            console.log('Access Denied. Redirecting to login...');
            window.history.pushState(null, '', '/login');
            return await this.route();
        }

        // Redirect authenticated users away from login/register
        if (this.authManager.isAuthenticated && (path === '/login' || path === '/register')) {
            console.log('Already logged in. Redirecting to home...');
            window.history.pushState(null, '', '/');
            return await this.route();
        }

        // Show loading
        this.uiManager.showLoading('content');

        // Sync cart
        await this.cartManager.syncWithServer();

        // Find route
        let routeAction = this.routes[path];

        if (!routeAction) {
            const editMatch = path.match(/^\/admin\/edit-product\/(\d+)$/);
            const detailMatch = path.match(/^\/product\/(\d+)$/);

            if (editMatch) {
                routeAction = () => this.routes['/admin/edit-product/:id']({ id: editMatch[1] });
            } else if (detailMatch) {
                routeAction = () => this.routes['/product/:id']({ id: detailMatch[1] });
            }
        }

        if (routeAction) {
            try {
                const html = await routeAction();
                document.getElementById('content').innerHTML = html;
                window.scrollTo(0, 0);
            } catch (error) {
                console.error('Routing error:', error);
                document.getElementById('content').innerHTML = '<h1>Error loading content</h1>';
            }
            return;
        }

        // Fallback to 404
        try {
            const html = await this.routes['/error']();
            document.getElementById('content').innerHTML = html;
        } catch (error) {
            console.error('Routing error:', error);
            document.getElementById('content').innerHTML = '<h1>Error loading content</h1>';
        }
    }
}