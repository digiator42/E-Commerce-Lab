let cartState = [];
let isCartOpen = false;

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

async function renderCartItems() {
    const container = document.getElementById('cart-items-list');
    const totalEl = document.getElementById('cart-total');

    if (cartState.length === 0) {
        container.innerHTML = '<p class="text-gray-500 text-center mt-10">Your cart is empty.</p>';
        totalEl.innerText = '$0.00';
        return;
    }

    let html = cartState.map((item, index) => `
        <div class="flex items-center justify-between border-b pb-4">
            <div>
                <h4 class="font-bold text-sm">${item.name}</h4>
                <p class="text-gray-500 text-xs">$${item.price.toFixed(2)}</p>
            </div>
            <div class="flex items-center space-x-2">
                <button onclick="removeFromCart(${index})" class="text-red-500 hover:bg-red-50 p-1 rounded">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                </button>
            </div>
        </div>
    `).join('');

    container.innerHTML = html;

    const total = cartState.reduce((sum, item) => sum + item.price, 0);
    totalEl.innerText = `$${total.toFixed(2)}`;
}

async function addToCart(productId) {

    const productElement = event.target.closest('.group');
    const name = productElement.querySelector('h3').innerText;
    const price = parseFloat(productElement.querySelector('.text-2xl').innerText.replace('$', ''));

    cartState.push({ id: productId, name: name, price: price });

    updateCartBadge();
    toggleCartDrawer();
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
    const res = await fetch('/api/cart');
    if (res.ok) {
        cartState = await res.json();
        renderCartItems();
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

        // Inject data into the card template
        const productListHtml = res.content.map(p => {
            return cardTemplate
                .replace(/{{name}}/g, p.name)
                .replace(/{{description}}/g, p.description)
                .replace(/{{price}}/g, p.price.toFixed(2))
                .replace(/{{id}}/g, p.id);
        }).join('');

        return template.replace('{{productList}}', productListHtml);
    },

    '/login': async () => {
        return await ComponentStore.load('login');
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
    const viewFunc = routes[path] || (() => '<h1>404 Not Found</h1>');

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