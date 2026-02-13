let cartState = [];
let isCartOpen = false;
let currentPage = 0;
let currentSearch = "";
let totalPages = 0;
let searchTimeout;

async function changePage(direction) {
    currentPage += direction;
    await fetchAndRenderProducts();
}

async function fetchAndRenderProducts() {
    const container = document.getElementById('product-list-container');
    const url = `/api/products?page=${currentPage}&size=6&search=${encodeURIComponent(currentSearch)}`;

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
            .replace(/{{id}}/g, p.id);
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
        router();
        return;
    }

    if (response.ok) {
        await syncCartWithServer();
        updateCartBadge();
        toggleCartDrawer();
    }
}

async function syncCartWithServer() {
    console.log("Syncing cart with server...");
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

const routes = {
    '/': async () => {
        return await ComponentStore.load('home');
    },

    '/products': async () => {
        const [template, cardTemplate, res] = await Promise.all([
            ComponentStore.load('products'),
            ComponentStore.load('product-card'),
            fetch('/api/products').then(r => r.json())
        ]);

        const productListHtml = res.content.map(p => {
            return cardTemplate
                .replace(/{{name}}/g, p.name)
                .replace(/{{description}}/g, p.description)
                .replace(/{{price}}/g, (p?.price ?? 0).toFixed(2))
                .replace(/{{id}}/g, p.id);
        }).join('');

        return template.replace('{{productList}}', productListHtml);
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
    '/error': async () => {
        return await ComponentStore.load('404');
    }
};

async function router(event) {
    if (event) {
        event.preventDefault();
        // Update the URL in the address bar
        const href = event.target.closest('a').getAttribute('href');
        window.history.pushState(null, "", href);
    }

    const path = window.location.pathname;
    const viewFunc = routes[path] || routes['/error'];

    document.getElementById('content').innerHTML = '<div class="spinner">Loading...</div>';

    try {
        const html = await viewFunc();
        document.getElementById('content').innerHTML = html;
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
        alert(`Welcome back, ${user.email}!`);
        window.history.pushState(null, "", "/");
        router();
    } else {
        alert("Login failed! Check your credentials.");
    }
}

async function handleLogout(event) {
    const response = await fetch('/api/auth/logout', {
        method: 'POST'
    });
    if (response.ok) {
        alert("You have been logged out.");
        window.history.pushState(null, "", "/login");
        router();
    } else {
        alert("Logout failed. Please try again.");
    }
}