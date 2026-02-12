const routes = {
    '/': () => `
        <div class="text-center py-20">
            <h1 class="text-5xl font-extrabold mb-4">Quality meets Convenience.</h1>
            <p class="text-xl text-gray-600 mb-8">Discover our curated collection of premium products.</p>
            <a href="/products" onclick="router(event)" class="bg-blue-600 text-white px-8 py-3 rounded-full text-lg font-semibold hover:bg-blue-700 shadow-lg transition">Start Shopping</a>
        </div>`,

    '/products': async () => {
        const res = await fetch('/api/products');
        const data = await res.json();

        return `
            <div class="flex justify-between items-center mb-8">
                <h2 class="text-3xl font-bold">Our Catalog</h2>
                <span class="text-gray-500">${data.totalElements} Products found</span>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                ${data.content.map(p => `
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition">
                        <div class="h-48 bg-gray-200 flex items-center justify-center text-gray-400">
                             <svg class="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                        </div>
                        <div class="p-6">
                            <h3 class="text-xl font-bold mb-2">${p.name}</h3>
                            <p class="text-gray-600 text-sm mb-4 line-clamp-2">${p.description}</p>
                            <div class="flex justify-between items-center">
                                <span class="text-2xl font-bold text-blue-600">$${p.price.toFixed(2)}</span>
                                <button onclick="addToCart(${p.id})" class="bg-gray-900 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition">Add to Cart</button>
                            </div>
                        </div>
                    </div>
                `).join('')}
            </div>`;
    },

    '/login': () => `
        <div class="max-w-md mx-auto bg-white p-8 rounded-2xl shadow-xl mt-12">
            <h2 class="text-2xl font-bold mb-6 text-center">Sign In</h2>
            <form onsubmit="handleLogin(event)" class="space-y-4">
                <div>
                    <label class="block text-sm font-medium mb-1">Email</label>
                    <input name="email" type="email" class="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 outline-none">
                </div>
                <div>
                    <label class="block text-sm font-medium mb-1">Password</label>
                    <input name="password" type="password" class="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 outline-none">
                </div>
                <button type="submit" class="w-full bg-blue-600 text-white py-3 rounded-lg font-bold hover:bg-blue-700 transition">Enter Store</button>
            </form>
        </div>`
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