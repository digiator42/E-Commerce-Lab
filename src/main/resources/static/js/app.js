let cartState = [];
let isCartOpen = false;
let currentPage = 0;
let currentSearch = "";
let totalPages = 0;
let searchTimeout;
let currentCategory = null;
let selectedRating = 0;

const statusColors = {
    'PENDING': 'bg-yellow-100 text-yellow-700',
    'SHIPPED': 'bg-blue-100 text-blue-700',
    'DELIVERED': 'bg-green-100 text-green-700',
    'CANCELLED': 'bg-red-100 text-red-700'
};

let isAuth = false;

async function startApp() {
    console.log("Checking authentication...");
    isAuth = await isLoggedIn();

    console.log("Auth check complete. isAuth:", isAuth);

    const user = JSON.parse(localStorage.getItem('user'));
    console.log("Current User:", user);

    await router();
    initData(user);
}

startApp();

function showToast(msg, duration = 3000) {
    const toast = document.createElement('div');
    toast.className = 'fixed bottom-4 right-4 bg-gray-900 text-white px-6 py-3 rounded-lg shadow-lg animate-fade-in z-50';
    toast.innerText = msg;
    
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.classList.add('opacity-0', 'transition-opacity');
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

function toggleAuthButtons() {
    const loginBtn = document.getElementById('login-btn');
    const logoutBtn = document.getElementById('logout-btn');

    if (isAuth) {
        loginBtn?.classList.add('hidden');
        logoutBtn?.classList.remove('hidden');
    } else {
        loginBtn?.classList.remove('hidden');
        logoutBtn?.classList.add('hidden');
    }
}

const initData = (user) => {
    const userNameElement = document.getElementById('userName');
    if (user) {
        const userName = user.email.split('@')[0];
        if (userNameElement) {
            userNameElement.innerText = userName;
        }
    }
    else {
        if (userNameElement) {
            userNameElement.innerText = "Guest";
        }
    }
}

function setRating(n) {
    selectedRating = n;
    const stars = document.querySelectorAll('#star-rating-input button');
    stars.forEach((s, i) => {
        s.style.color = i < n ? '#FBBF24' : '#D1D5DB'; // Yellow or Gray
    });
}

async function submitReview(productId) {
    const comment = document.getElementById('review-comment').value;
    if (selectedRating === 0) return alert("Please select a star rating");

    const res = await fetch(`/api/reviews/${productId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ rating: selectedRating, comment: comment })
    });

    if (res.ok) {
        location.reload();
    } else {
        alert("You must be logged in to review!");
    }
}

async function filterCategory(categoryName) {
    currentCategory = categoryName;
    currentPage = 0;

    document.querySelectorAll('#category-list button').forEach(btn => {
        btn.classList.remove('bg-blue-50', 'text-blue-600', 'font-bold');
    });
    event.target.classList.add('bg-blue-50', 'text-blue-600', 'font-bold');

    await fetchAndRenderProducts();
}

async function changePage(direction) {
    currentPage += direction;
    await fetchAndRenderProducts();
}

async function fetchAndRenderProducts() {
    const container = document.getElementById('product-list-container');

    let url = `/api/products?page=${currentPage}&size=6`;
    if (currentSearch) url += `&search=${encodeURIComponent(currentSearch)}`;
    if (currentCategory) url += `&category=${encodeURIComponent(currentCategory)}`;

    window.history.pushState(null, "", url.substring(5));

    const res = await fetch(url);
    const data = await res.json(); // This is the Page object

    totalPages = data.totalPages;

    // Update Pagination Buttons
    document.getElementById('prev-btn').disabled = data.first;
    document.getElementById('next-btn').disabled = data.last;
    document.getElementById('page-info').innerText = `Page ${data.number + 1} of ${data.totalPages}`;

    const cardTemplate = await ComponentStore.load('product-card');

    container.innerHTML = data.content.map(p => {
        return cardTemplate
            .replace(/{{name}}/g, p.name)
            .replace(/{{description}}/g, p.description)
            .replace(/{{price}}/g, p.price.toFixed(2))
            .replace(/{{id}}/g, p.id)
            .replace(/{{category}}/g, p.category);
    }).join('');
}

async function handleSearch(event) {
    currentSearch = event.target.value;
    currentPage = 0; // Reset to first page on new search

    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        fetchAndRenderProducts();
    }, 300);
}

async function deleteProduct(id) {
    if (!confirm("Are you sure? This will permanently remove this product.")) return;

    const res = await fetch(`/api/admin/products/${id}`, {
        method: 'DELETE'
    });

    if (res.ok) {
        await router();
    } else {
        alert("Failed to delete product. It might be linked to existing orders.");
    }
}

async function editProduct(id) {
    window.history.pushState({}, "", `/admin/edit-product/${id}`);
    await router();
}

async function updateProduct(event, id) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData.entries());

    try {
        const res = await fetch(`/api/admin/products/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (res.ok) {
            alert("Product updated successfully!");
            window.history.pushState({}, "", "/admin");
            await router();
        } else {
            const error = await res.text();
            alert("Update failed: " + error);
        }
    } catch (err) {
        console.error("Update Error:", err);
    }
}

async function saveProduct(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const productData = Object.fromEntries(formData.entries());

    console.log("Submitting Product:", productData);

    const res = await fetch('/api/admin/products', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(productData)
    });

    if (res.ok) {
        alert("Product Added Successfully!");
        window.history.pushState({}, "", "/admin");
        await router();
    } else {
        const err = await res.text();
        alert("Error: " + err);
    }
}

async function updateOrderStatus(orderId, newStatus) {

    const res = await fetch(`/api/admin/orders/${orderId}/status?status=${newStatus}`, {
        method: 'PATCH'
    });

    if (res.ok) {

        const badge = document.getElementById(`status-badge-${orderId}`);
        if (badge) {
            badge.innerText = newStatus;
            badge.className = `inline-block w-fit px-3 py-1 rounded-full text-[10px] font-bold uppercase ${statusColors[newStatus]}`;
            showToast(`Order #${orderId} status updated to ${newStatus}`);
        }
    } else {
        alert("Failed to update status on the server.");
    }
}

async function switchAdminTab(tab) {

    const isOrders = tab === 'orders';
    document.getElementById('section-inventory').classList.toggle('hidden', isOrders);
    document.getElementById('section-orders').classList.toggle('hidden', !isOrders);

    document.getElementById('tab-inventory').className = !isOrders ? 'pb-4 px-2 border-b-2 border-blue-600 font-bold text-blue-600' : 'pb-4 px-2 text-gray-400 font-bold';
    document.getElementById('tab-orders').className = isOrders ? 'pb-4 px-2 border-b-2 border-blue-600 font-bold text-blue-600' : 'pb-4 px-2 text-gray-400 font-bold';

    if (isOrders) {
        const res = await fetch('/api/admin/orders');
        const orders = await res.json();

        const tableBody = document.getElementById('admin-orders-table');
        tableBody.innerHTML = orders.map(order => {

            const itemsList = order.items.map(i => `${i.productName} (x${i.quantity})`).join(', ');

            const status = order.status ? order.status : '';

            return `
                <tr class="border-b border-gray-50 hover:bg-gray-50 transition">
                    <td class="py-4 px-6 font-mono text-xs">#ORD-${order.id}</td>
                    <td class="py-4 px-2">
                        <div class="text-sm font-medium text-gray-900">${order.user.email}</div>
                        <div class="text-[10px] text-gray-400">${new Date(order.orderDate).toLocaleDateString()}</div>
                    </td>
                    <td class="py-4 px-2">
                        <div class="flex flex-col gap-2">
                            <span id="status-badge-${order.id}" class="inline-block w-fit px-3 py-1 rounded-full text-[10px] font-bold uppercase ${statusColors[status]}">
                                ${status ? status : 'PENDING'}
                            </span>
                            <select onchange="updateOrderStatus(${order.id}, this.value)" 
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
}

syncCartWithServer(); // Initial sync on page load

function toggleCartDrawer() {
    const drawer = document.getElementById('cart-drawer');
    const overlay = document.getElementById('cart-overlay');

    isCartOpen = !isCartOpen;

    if (isCartOpen) {
        drawer.classList.remove('translate-x-full');
        overlay.classList.remove('hidden');
        setTimeout(() => overlay.classList.add('opacity-100'), 10);
        renderCartItems();
    } else {
        drawer.classList.add('translate-x-full');
        overlay.classList.remove('opacity-100');
        setTimeout(() => overlay.classList.add('hidden'), 300);
    }
}

async function checkout() {
    const checkoutBtn = event.target;

    if (cartState.length === 0) return alert("Your cart is empty!");

    // Prevent double clicks
    checkoutBtn.disabled = true;
    checkoutBtn.innerText = "Processing...";

    try {
        const response = await fetch('/api/orders/place', {
            method: 'POST'
        });

        if (response.ok) {

            cartState = [];

            updateCartBadge();
            toggleCartDrawer();

            document.getElementById('content').innerHTML = `
                <div class="text-center py-20 animate-fade-in">
                    <div class="bg-green-100 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
                        <svg class="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path></svg>
                    </div>
                    <h2 class="text-4xl font-black text-gray-900 mb-4">Success!</h2>
                    <p class="text-gray-600 text-lg mb-8">Your order has been received and is being processed.</p>
                    <button onclick="window.location.href='/products'; router(event);" class="bg-blue-600 text-white px-8 py-3 rounded-xl font-bold hover:bg-black transition">
                        Back to Shop
                    </button>
                </div>
            `;
            checkoutBtn.disabled = false;
            checkoutBtn.innerText = "Checkout";
        } else {
            const error = await response.text();
            alert("Checkout failed: " + error);
            checkoutBtn.disabled = false;
            checkoutBtn.innerText = "Checkout";
        }
    } catch (err) {
        console.error("Checkout Error:", err);
        checkoutBtn.disabled = false;
        checkoutBtn.innerText = "Checkout";
    }
}

async function renderCartItems() {
    const container = document.getElementById('cart-items-list');
    const totalEl = document.getElementById('cart-total');

    if (!cartState || cartState.length === 0) {
        container.innerHTML = '<p class="text-gray-500 text-center mt-10 font-medium">Your cart is lonely...</p>';
        totalEl.innerText = '$0.00';
        document.getElementById('cart-count').innerText = '0';
        return;
    }

    let html = cartState.map((item) => {

        const product = item.product;
        const itemTotal = (product.price * item.quantity).toFixed(2);

        return `
            <div class="flex items-center justify-between border-b border-gray-100 pb-4 group">
                <div class="flex-grow">
                    <h4 class="font-bold text-sm text-gray-800">${product.name}</h4>
                    <div class="flex items-center space-x-2 mt-1">
                        <span class="text-blue-600 font-semibold text-xs">$${product.price.toFixed(2)}</span>
                        <span class="text-gray-400 text-[10px]">x ${item.quantity}</span>
                    </div>
                </div>
                <div class="flex flex-col items-end space-y-2">
                    <span class="font-bold text-sm text-gray-900">$${itemTotal}</span>
                    <button onclick="removeFromCart(${item.id})" class="text-gray-400 hover:text-red-500 transition-colors p-1">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                        </svg>
                    </button>
                </div>
            </div>
        `;
    }).join('');

    container.innerHTML = html;

    const total = cartState.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);
    totalEl.innerText = `$${total.toFixed(2)}`;

    const totalQty = cartState.reduce((sum, item) => sum + item.quantity, 0);
    document.getElementById('cart-count').innerText = totalQty;
}

async function removeFromCart(cartItemId) {
    const response = await fetch(`/api/cart/remove/${cartItemId}`, {
        method: 'DELETE'
    });

    if (response.ok) {
        await syncCartWithServer(); // Refresh state from DB
    }
}

async function clearAllItems() {
    if (!confirm("Are you sure you want to empty your cart?")) return;

    const response = await fetch('/api/cart/clear', {
        method: 'DELETE'
    });

    if (response.ok) {
        cartState = [];
        renderCartItems();
        updateCartBadge();
        // toggleCartDrawer(); 
    }
}

async function addToCart(productId) {
    const response = await fetch(`/api/cart/add/${productId}`, {
        method: 'POST'
    });

    if (response.status === 401) {
        alert("Please log in to add items to your cart.");
        window.history.pushState(null, "", "/login");
        await router();
        return;
    }

    if (response.ok) {
        await syncCartWithServer();
        updateCartBadge();
        toggleCartDrawer();
    }
}

async function syncCartWithServer() {
    // console.log("Syncing cart with server...");
    const res = await fetch('/api/cart');
    if (res.ok) {
        cartState = await res.json(); // This is now a List<CartItem>
        renderCartItems();

        const badge = document.getElementById('cart-count');
        const totalQty = cartState.reduce((sum, item) => sum + item.quantity, 0);
        badge.innerText = totalQty;
        totalQty > 0 ? badge.classList.remove('hidden') : badge.classList.add('hidden');
    }
}

function updateCartBadge() {
    const badge = document.getElementById('cart-count');
    if (badge) {
        let current = parseInt(badge.innerText) || 0;
        badge.innerText = current + 1;
        badge.classList.remove('hidden');
    }
}

const ComponentStore = {
    templates: {},

    async load(name) {
        if (!this.templates[name]) {
            try {
                const res = await fetch(`/components/${name}.html`);
                if (!res.ok) throw new Error(`Component ${name} not found (Status: ${res.status})`);
                this.templates[name] = await res.text();
            } catch (err) {
                console.error("Template Load Error:", err);
                // Return a fallback UI
                return `<div class="p-8 text-red-500 bg-red-50 rounded-lg">
                            <strong>Error:</strong> Could not load component "${name}".
                        </div>`;
            }
        }
        return this.templates[name];
    }
};

async function navigateToAddProduct() {
    window.history.pushState({}, "", "/admin/add-product");

    await router();
}

function getMethodColor(method) {
    switch (method) {
        case 'GET': return 'bg-green-100 text-green-700';
        case 'POST': return 'bg-blue-100 text-blue-700';
        case 'PUT': return 'bg-yellow-100 text-yellow-700';
        case 'DELETE': return 'bg-red-100 text-red-700';
        default: return 'bg-gray-100 text-gray-700';
    }
}

const routes = {
    '/': async () => {
        return await ComponentStore.load('home');
    },

    '/admin': async () => {
        const template = await ComponentStore.load('admin-dashboard');

        // Fetch stats and the full product list
        const [stats, productsPage] = await Promise.all([
            fetch('/api/admin/stats').then(r => r.json()),
            fetch('/api/products?size=100').then(r => r.json())
        ]);

        const productRows = productsPage.content.map(p => `
        <tr class="border-b border-gray-50 hover:bg-gray-50 transition">
            <td class="py-4 px-2 font-medium text-gray-900">${p.name}</td>
            <td class="py-4 px-2 text-gray-500">${p.category}</td>
            <td class="py-4 px-2 font-bold text-blue-600">$${p.price.toFixed(2)}</td>
            <td class="py-4 px-2">
                <div class="flex space-x-2">
                    <button onclick="editProduct(${p.id})" class="text-gray-400 hover:text-blue-600">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path></svg>
                    </button>
                    <button onclick="deleteProduct(${p.id})" class="text-gray-400 hover:text-red-600">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');

        return template
            .replace('{{totalRevenue}}', stats.revenue.toFixed(2))
            .replace('{{totalOrders}}', stats.orders)
            .replace('{{totalProducts}}', stats.products)
            .replace('{{productRows}}', productRows);
    },

    '/admin/add-product': async () => {
        const [template, categoryRes] = await Promise.all([
            ComponentStore.load('add-product'),
            fetch('/api/categories').then(r => r.json())
        ]);

        const categoryOptions = categoryRes.map(cat =>
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
            const res = await fetch('/api/admin/routes');
            if (res.ok) {
                const data = await res.json();
                const rowsHtml = data.map(route => `
                <tr class="hover:bg-gray-50">
                    <td class="px-6 py-4 font-mono text-sm text-blue-600">
                        ${Array.isArray(route.path) ? route.path.join(', ') : route.path}
                    </td>
                    <td class="px-6 py-4">
                        ${route.methods.map(m => `
                            <span class="px-2 py-1 text-xs font-bold rounded ${getMethodColor(m)}">${m}</span>
                        `).join('')}
                    </td>
                    <td class="px-6 py-4 text-sm text-gray-600">${route.expectedStatus}</td>
                    <td class="px-6 py-4 text-xs text-gray-400 italic">${route.handler}</td>
                </tr>
            `).join('');
                document.getElementById('api-route-rows').innerHTML = rowsHtml;
            }
        }, 0);

        return template;
    },

    '/products': async () => {
        // Fetch everything in parallel
        const [template, cardTemplate, productRes, categoryRes] = await Promise.all([
            ComponentStore.load('products'),
            ComponentStore.load('product-card'),
            fetch(
                `/api/products?page=${currentPage}&size=6&search=${encodeURIComponent(currentSearch)}${currentCategory ? `&category=${encodeURIComponent(currentCategory)}` : ''}`
            ).then(r => r.json()).catch(err => {
                console.error("Product Fetch Error:", err);
                return { content: [], totalPages: 0 };
            }),
            fetch('/api/categories').then(r => r.json())
        ]);

        console.log("Fetched Products: ======> ", productRes);
        if (productRes.status === 401) {
            window.history.pushState(null, "", "/login");
            isAuth = false;
            return await router();
        }

        // Product List
        const productListHtml = productRes.content.map(p => {
            return cardTemplate
                .replace(/{{name}}/g, p.name)
                .replace(/{{description}}/g, p.description)
                .replace(/{{price}}/g, (p?.price ?? 0).toFixed(2))
                .replace(/{{id}}/g, p.id)
                .replace(/{{category}}/g, p.category);
        }).join('');

        // Category Sidebar HTML
        const categoryHtml = `
        <button onclick="filterCategory(null)" 
            class="w-full text-left px-4 py-2 rounded-xl transition ${!currentCategory ? 'bg-blue-50 text-blue-600 font-bold' : 'text-gray-500 hover:bg-gray-50'}">
            All Products
        </button>
    ` + categoryRes.map(cat => `
        <button onclick="filterCategory('${cat.name}')" 
            class="w-full text-left px-4 py-2 rounded-xl transition ${currentCategory === cat.name ? 'bg-blue-50 text-blue-600 font-bold' : 'text-gray-500 hover:bg-gray-50'}">
            ${cat.icon} ${cat.name}
        </button>
        
    `).join('');

        let finalHtml = template.replace('{{productList}}', productListHtml);

        // We use a temporary DOM element to perform the sidebar injection before returning the string
        const parser = new DOMParser();
        const doc = parser.parseFromString(finalHtml, 'text/html');
        const catContainer = doc.getElementById('category-list');
        if (catContainer) catContainer.innerHTML = categoryHtml;

        return doc.body.innerHTML;
    },

    '/login': async () => {
        return await ComponentStore.load('login');
    },

    '/orders': async () => {
        const template = await ComponentStore.load('orders');
        const res = await fetch('/api/orders/my-orders');
        const orders = await res.json();

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
    },

    '/product/:id': async (params) => {
        const template = await ComponentStore.load('product-detail');
        const res = await fetch(`/api/products/${params.id}`);
        const p = await res.json();

        // HTML for the reviews list
        const reviewsHtml = p.reviews.length > 0 ? p.reviews.map(rev => `
        <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
            <div class="flex items-center justify-between mb-2">
                <span class="font-bold text-sm text-gray-900">${rev.userEmail}</span>
                <span class="text-yellow-400 font-bold">${'★'.repeat(rev.rating)}</span>
            </div>
            <p class="text-gray-600 text-sm">${rev.comment}</p>
            <p class="text-[10px] text-gray-400 mt-2">${new Date(rev.date).toLocaleDateString()}</p>
        </div>
    `).join('') : '<p class="text-gray-400 italic">No reviews yet. Be the first!</p>';

        return template
            .replace(/{{name}}/g, p.name)
            .replace(/{{description}}/g, p.description)
            .replace(/{{price}}/g, p.price.toFixed(2))
            .replace(/{{category}}/g, p.categoryName)
            .replace(/{{id}}/g, p.id)
            .replace('{{reviewsHtml}}', reviewsHtml);
    },

    '/admin/edit-product/:id': async (params) => {

        const [template, product, categories] = await Promise.all([
            ComponentStore.load('add-product'),
            fetch(`/api/products/${params.id}`).then(r => r.json()),
            fetch('/api/categories').then(r => r.json())
        ]);

        const options = categories.map(c => `
        <option value="${c.name}" ${c.name === product.categoryName ? 'selected' : ''}>
            ${c.name}
        </option>
    `).join('');

        return template
            .replace('Create New Product', `Edit Product: ${product.name}`)
            .replace('{{categoryOptions}}', options)
            .replace('name="name"', `name="name" value="${product.name}"`)
            .replace('name="price"', `name="price" value="${product.price}"`)
            .replace('name="description"', `name="description"`)
            .replace('name="stock"', `name="stock" value="${product.stock}"`)
            .replace('</textarea>', `${product.description}</textarea>`)
            .replace('onsubmit="saveProduct(event)"', `onsubmit="updateProduct(event, ${product.id})"`);
    },

    '/error': async () => {
        return await ComponentStore.load('404');
    }
};

async function isLoggedIn() {
    const response = await fetch('/api/auth/is-logged-in');
    console.log("Auth Check Response:", response);
    return response.ok;
}

const publicPaths = ['/', '/login', '/register'];

async function router(event) {
    if (event) {
        event.preventDefault();
        // Update the URL in the address bar
        const href = event.target.closest('a').getAttribute('href');
        window.history.pushState(null, "", href);
    }

    toggleAuthButtons();

    const path = window.location.pathname;

    console.log("isAuth:", isAuth);

    if (!isAuth && !publicPaths.includes(path)) {
        console.log("Access Denied. Redirecting to login...");
        window.history.pushState(null, "", "/login");
        return await router();
    }

    if (isAuth && (path === '/login' || path === '/register')) {
        console.log("Already logged in. Redirecting to home...");
        window.history.pushState(null, "", "/");
        return await router();
    }

    let routeAction = routes[path];

    console.log("Routing to:", routeAction);

    if (!routeAction) {
        const editMatch = path.match(/^\/admin\/edit-product\/(\d+)$/);
        const detailMatch = path.match(/^\/product\/(\d+)$/);

        console.log("Dynamic Route Check:", { path, editMatch, detailMatch });

        if (editMatch) {
            routeAction = () => routes['/admin/edit-product/:id']({ id: editMatch[1] });
            console.log("Routing to:", routeAction);
        } else if (detailMatch) {
            routeAction = () => routes['/product/:id']({ id: detailMatch[1] });
        }
    }

    if (routeAction) {
        const html = await routeAction();
        document.getElementById('content').innerHTML = html;

        window.scrollTo(0, 0);
        return;
    }

    const viewFunc = routes[path] || routes['/error'];

    document.getElementById('content').innerHTML = '<div class="spinner">Loading...</div>';

    try {
        const html = await viewFunc();
        document.getElementById('content').innerHTML = html;
        // initData();
    } catch (error) {
        console.error("Routing error:", error);
        document.getElementById('content').innerHTML = '<h1>Error loading content</h1>';
    }
}

async function handleLogin(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData.entries());

    const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });

    if (response.ok) {
        const user = await response.json();
        // alert(`Welcome back, ${user.email}!`);
        isAuth = true;
        window.history.pushState(null, "", "/");
        await router();
        localStorage.setItem('user', JSON.stringify(user));
        document.getElementById('userName').innerText = user.email.split('@')[0];
    } else {
        alert("Login failed! Check your credentials.");
    }
}

async function handleLogout(event) {
    const response = await fetch('/api/auth/logout', {
        method: 'POST'
    });
    if (response.ok) {
        // alert("You have been logged out.");
        localStorage.removeItem('user');
        window.history.pushState(null, "", "/login");
        isAuth = false;
        await router();
    } else {
        alert("Logout failed. Please try again.");
    }
}