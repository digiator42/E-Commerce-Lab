export class User {
    constructor(data = {}) {
        this.id = data.id || null;
        this.token = data.token || '';
        this.email = data.email || '';
        this.displayName = data.displayName || '';
        this.userName = data.userName || '';
        this.role = data.role || 'ROLE_USER';
        this.defaultAddress = data.defaultAddress || null;
        this.profilePicture = data.profilePicture || null;
        this.lastLogin = new Date(data.lastLogin).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        }) || new Date.now().toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
        this.isConstructorized = true;
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