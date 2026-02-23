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

    setUserManager(userManager) {
        this.userManager = userManager;
    }

    setWishlistManager(wishlistManager) {
        this.wishlistManager = wishlistManager;
    }

    getPublicPaths() {
        return [
            '/',
            '/login',
            '/register',
            '/products',
            '/product/',
            '/categories'
        ];
    }

    getProtectedPaths() {
        return [
            '/orders',
            '/cart',
            '/checkout',
            '/profile',
            '/wishlist'
        ];
    }

    pathMatchesPattern(path, pattern) {
        if (pattern.endsWith('/')) {
            return path.startsWith(pattern);
        }
        return path === pattern;
    }

    initRoutes() {

        if (!this.authManager) {
            console.error('AuthManager not set in Router');
            return {};
        }

        return {
            '/': async () => {
                const template = await this.componentStore.load('home');

                // Load products for each category after rendering
                setTimeout(async () => {
                    await this.productManager.loadCategoryProducts('Electronics', 'electronics-products');
                    await this.productManager.loadCategoryProducts('Clothing', 'fashion-products');
                    await this.productManager.loadCategoryProducts('Home %26 Garden', 'home-products');
                }, 0);

                return template;
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

            '/profile': async () => {

                // Protected route
                if (!this.authManager?.isAuthenticated) {
                    window.history.pushState(null, '', '/login');
                    return await this.route();
                }

                const template = await this.componentStore.load('profile');

                setTimeout(async () => {
                    const user = this.authManager.user;
                    console.log('Rendering profile for user:', user);
                    if (user) {
                        // Set display name
                        const displayNameInput = document.getElementById('display-name');
                        if (displayNameInput) {
                            displayNameInput.value = user.displayName || '';
                        }

                        // Set email
                        const emailInput = document.getElementById('profile-email-input');
                        if (emailInput) {
                            emailInput.value = user.email || '';
                        }

                        // Set profile initials
                        const initials = document.getElementById('profile-initials');
                        if (initials) {
                            const name = user.displayName || user.email || 'User';
                            initials.textContent = name.charAt(0).toUpperCase();
                        }

                        // Set display name in sidebar
                        const displayNameEl = document.getElementById('profile-display-name');
                        if (displayNameEl) {
                            displayNameEl.textContent = user.displayName || user.email?.split('@')[0] || 'User';
                        }

                        // Set email in sidebar
                        const profileEmail = document.getElementById('profile-email');
                        if (profileEmail) {
                            profileEmail.textContent = user.email || '';
                        }

                        // Parse and set address if exists
                        if (user.defaultAddress) {
                            try {
                                const address = JSON.parse(user.defaultAddress);
                                document.getElementById('address-street').value = address.street || '';
                                document.getElementById('address-city').value = address.city || '';
                                document.getElementById('address-state').value = address.state || '';
                                document.getElementById('address-zip').value = address.zipCode || '';
                                document.getElementById('address-country').value = address.country || 'USA';
                            } catch (e) {
                                // If not JSON, treat as plain string
                                document.getElementById('address-street').value = user.defaultAddress;
                            }
                        }
                    }
                }, 0);

                return template;
            },

            '/admin/add-product': async () => {
                if (!this.authManager?.isAdmin()) {
                    window.history.pushState(null, '', '/products');
                    return await this.route();
                }
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
                if (!this.authManager?.isAdmin()) {
                    window.history.pushState(null, '', '/products');
                    return await this.route();
                }
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
                // Public route
                window.productManager.loadFiltersFromURL();
                const template = await this.componentStore.load('products');
                const categoryRes = await fetch('/api/categories').then(res => res.json());

                // Build category sidebar HTML
                const categoryHtml = categoryRes.map(cat => `
                    <label class="flex items-center space-x-3 p-2 rounded-xl hover:bg-gray-50 cursor-pointer transition">
                        <input type="checkbox" 
                            onchange="window.productManager.toggleCategoryFilter('${cat.name.trim()}')" 
                            class="w-4 h-4 text-blue-600 rounded border-gray-300 focus:ring-blue-500 category-checkbox"
                            data-category="${cat.name.trim()}"
                            ${window.productManager.selectedCategories.has(cat.name.trim()) ? 'checked' : ''}>
                        <span class="text-sm font-medium text-gray-700">${cat.icon || ''} ${cat.name}</span>
                    </label>
                `).join('');

                const allProductsHtml = `
                    <label class="flex items-center space-x-3 p-2 rounded-xl hover:bg-gray-50 cursor-pointer transition">
                        <input type="checkbox" 
                            onchange="window.productManager.toggleCategoryFilter(null)" 
                            class="w-4 h-4 text-blue-600 rounded border-gray-300 focus:ring-blue-500 category-checkbox"
                            data-category="all"
                            ${window.productManager.selectedCategories.size === 0 ? 'checked' : ''}>
                        <span class="text-sm font-medium text-gray-700">All Products</span>
                    </label>
                `;

                let finalHtml = template.replace(
                    '<div id="category-list" class="space-y-2 min-h-60 overflow-y-auto pr-2">',
                    `<div id="category-list" class="space-y-2 min-h-60 overflow-y-auto pr-2">
                        ${allProductsHtml}
                        ${categoryHtml}`
                );

                const parser = new DOMParser();
                const doc = parser.parseFromString(finalHtml, 'text/html');

                // Set UI elements from filters
                const searchInput = doc.getElementById('product-search');
                if (searchInput) searchInput.value = window.productManager.currentSearch;

                const sortSelect = doc.getElementById('products-filter');
                if (sortSelect) sortSelect.value = window.productManager.sortBy;

                const minRange = doc.getElementById('min-price-range');
                const maxRange = doc.getElementById('max-price-range');
                const minInput = doc.getElementById('min-price-input');
                const maxInput = doc.getElementById('max-price-input');

                if (minRange) minRange.value = window.productManager.priceRange.min;
                if (maxRange) maxRange.value = window.productManager.priceRange.max;
                if (minInput) minInput.value = window.productManager.priceRange.min;
                if (maxInput) maxInput.value = window.productManager.priceRange.max;

                const ratingRadio = doc.querySelector(`input[name="rating-filter"][value="${window.productManager.minRating}"]`);
                if (ratingRadio) {
                    ratingRadio.checked = true;
                } else {
                    const allRating = doc.querySelector('input[name="rating-filter"][value="0"]');
                    if (allRating) allRating.checked = true;
                }

                const stockCheckbox = doc.getElementById('in-stock-filter');
                if (stockCheckbox) stockCheckbox.checked = window.productManager.inStockOnly;

                setTimeout(async () => {
                    await window.productManager.renderProducts();
                    window.productManager.updateRatingCounts();
                    window.productManager.updateActiveFilters();
                }, 0);

                return doc.body.innerHTML;
            },

            '/login': async () => {
                return await this.componentStore.load('login');
            },

            '/register': async () => {
                // If already authenticated, redirect to home
                if (this.authManager?.isAuthenticated) {
                    window.history.pushState(null, '', '/');
                    return await this.route();
                }
                return await this.componentStore.load('register');
            },

            '/orders': async () => {
                return await this.orderManager.renderOrders();
            },

            '/product/:id': async (params) => {
                // Public route
                let template = await this.componentStore.load('product-detail');
                try {
                    const p = await fetch(`/api/products/${params.id}`).then(res => res.json());

                    const canReview = p.reviewStatus === "CAN_REVIEW" ? true : false;

                    if (!canReview) {
                        // Hide review form if user cannot review (either not purchased or already reviewed)
                        template = template.replace('can-review-container mt-16 border-t pt-10', 'can-review-container hidden');
                    }

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
                } catch (error) {
                    console.error('Error loading product:', error);
                    return '<div class="text-center py-10">Product not found</div>';
                }
            },

            '/admin/edit-product/:id': async (params) => {
                if (!this.authManager?.isAdmin()) {
                    window.history.pushState(null, '', '/products');
                    return await this.route();
                }
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
                    .replace('saveProduct(event)', `updateProduct(event, ${product.id})`)
                    .replace('Save Product', 'Update Product');
            },

            '/gift-cards': async () => {
                return `
                    <div class="max-w-4xl mx-auto text-center py-16">
                        <h1 class="text-5xl font-black mb-6">🎁 Gift Cards</h1>
                        <p class="text-xl text-gray-600 mb-12">The perfect gift for any occasion</p>
                        
                        <div class="grid md:grid-cols-3 gap-8 mb-12">
                            ${[25, 50, 100].map(amount => `
                                <div class="bg-white rounded-2xl shadow-lg p-8 border border-gray-100 hover:shadow-2xl transition cursor-pointer" onclick="window.cartManager.addGiftCard(${amount})">
                                    <div class="text-6xl mb-4">🎫</div>
                                    <h3 class="text-3xl font-bold text-gray-900 mb-2">$${amount}</h3>
                                    <p class="text-gray-500 mb-4">Digital delivery</p>
                                    <button class="bg-blue-600 text-white px-6 py-3 rounded-xl font-bold hover:bg-blue-700 transition w-full">
                                        Add to Cart
                                    </button>
                                </div>
                            `).join('')}
                        </div>
                        
                        <div class="bg-gradient-to-r from-purple-500 to-indigo-600 text-white p-8 rounded-2xl">
                            <h3 class="text-2xl font-bold mb-2">Custom Amount</h3>
                            <p class="mb-4">Choose your own amount between $10 and $500</p>
                            <div class="flex justify-center space-x-4">
                                <input type="number" id="custom-amount" min="10" max="500" step="5" 
                                    class="px-4 py-3 rounded-xl text-gray-900 w-48" placeholder="Enter amount">
                                <button onclick="window.cartManager.addGiftCard(document.getElementById('custom-amount').value)" 
                                    class="bg-white text-purple-600 px-8 py-3 rounded-xl font-bold hover:bg-purple-50 transition">
                                    Add to Cart
                                </button>
                            </div>
                        </div>
                    </div>
                `;
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

        this.uiManager.toggleAuthButtons(this.authManager?.isAuthenticated || false);

        let path = window.location.pathname;
        if (path.endsWith('/') && path.length > 1) {
            path = path.slice(0, -1);
        }

        console.log('Routing to path:', path);
        console.log('Is authenticated:', this.authManager?.isAuthenticated);

        // Check if path requires authentication
        const requiresAuth = this.getProtectedPaths().some(protectedPath =>
            this.pathMatchesPattern(path, protectedPath)
        );

        // Check if path is public
        const isPublic = this.getPublicPaths().some(publicPath =>
            this.pathMatchesPattern(path, publicPath)
        );

        // Admin access check
        if (path.startsWith('/admin') && !this.authManager?.isAdmin()) {
            console.error('User is not an admin. Redirecting to products...');
            window.history.pushState(null, '', '/products');
            return await this.route();
        }

        // Authentication check for protected routes
        if (requiresAuth && !this.authManager?.isAuthenticated) {
            console.log('Access Denied. Redirecting to login...');
            // Save the attempted URL to redirect back after login
            sessionStorage.setItem('redirectAfterLogin', path);
            window.history.pushState(null, '', '/login');
            return await this.route();
        }

        // Redirect authenticated users away from login/register
        if (this.authManager?.isAuthenticated && (path === '/login' || path === '/register')) {
            console.log('Already logged in. Redirecting to home...');
            window.history.pushState(null, '', '/');
            return await this.route();
        }

        // Show loading
        this.uiManager.showLoading('content');

        // Only sync cart & wishlist if authenticated (since APIs require auth)
        if (this.authManager?.isAuthenticated) {
            try {
                await this.cartManager?.syncWithServer();
                await this.wishlistManager?.syncWithServer();
            } catch (error) {
                console.log('Error syncing user data:', error);
                // Don't block rendering if sync fails
            }
        }

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