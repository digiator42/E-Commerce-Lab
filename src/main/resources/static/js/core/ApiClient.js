export class ApiClient {
    constructor(authManager, router) {
        this.authManager = authManager;
        this.router = router;
    }

    async fetch(url, options = {}) {
        const response = await fetch(url, options);

        if (response.status === 401) {
            await this.authManager.logout();
            await this.router.navigate('/login');
            throw new Error('Unauthorized');
        }
        
        if (response.status === 403) {
            await this.router.navigate('/orders');
            throw new Error('Unauthorized');
        }

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || `Something went wrong, try again!`);
        }

        const contentLength = response.headers.get('content-length');
        if (response.status === 204 || contentLength === '0') return null;

        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        }

        return null;
    }
}