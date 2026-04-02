export class ApiClient {
    constructor(authManager, router) {
        this.authManager = authManager;
        this.router = router;
    }

    async fetch(url, options = {}) {
        if (!options.headers) {
            options.headers = {};
        }

        if (!options.headers['Authorization']) {
            options.headers['Authorization'] = `Bearer ${this.authManager.user?.token}`;
        }

        const response = await fetch(url, options);

        if (response.status === 401) {
            this.authManager.user = null;
            this.authManager.isAuthenticated = false;
            // this.uiManager.toggleAuthButtons(false);

            // this will clear all stored data
            localStorage.clear();
            // await this.authManager.logout();
            await this.router.navigate('/login');
            throw new Error('Unauthorized');
        }

        if (response.status === 403) {
            await this.router.navigate('/products');
            throw new Error('Unauthorized');
        }

        if (!response.ok) {
            const error = await response.json();
            const errorMessage = error.message || error.error ||
                JSON.stringify(error).replace(/{|}|"/g, '').replace(':', ': ').toLocaleLowerCase();
            throw new Error(errorMessage || `Something went wrong, try again!`);
        }

        const contentLength = response.headers.get('content-length');
        if (response.status === 204 || contentLength === '0') return null;

        let contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        }

        contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('text/plain')) {
            return await response.text();
        }

        return null;
    }
}