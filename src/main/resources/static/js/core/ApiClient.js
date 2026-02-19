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
            return null;
        }

        if (response.status === 403) {
            await this.router.navigate('/orders');
            return null;
        }

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Error ${response.status}`);
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