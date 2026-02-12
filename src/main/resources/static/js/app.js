let cartState = [];

async function addToCart(productId) {
    console.log(`Adding product ${productId} to cart...`);

    const response = await fetch(`/api/cart/add/${productId}`, {
        method: 'POST'
    });

    if (response.status === 401) {
        alert("Please login first!");
        window.history.pushState(null, "", "/login");
        router();
        return;
    }

    if (response.ok) {
        updateCartBadge();
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