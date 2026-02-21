export class User {
    constructor(data = {}) {
        this.id = data.id || null;
        this.email = data.email || '';
        this.displayName = data.displayName || '';
        this.role = data.role || 'ROLE_USER';
        this.defaultAddress = data.defaultAddress || null;
    }

    isAdmin() {
        return this.role === 'ROLE_ADMIN';
    }

    getDisplayName() {
        return this.displayName || (this.email ? this.email.split('@')[0] : 'Guest');
    }

    hasAddress() {
        return this.defaultAddress !== null && this.defaultAddress !== '';
    }

    toUpdateDTO() {
        return {
            displayName: this.displayName,
            defaultAddress: this.defaultAddress
        };
    }
}