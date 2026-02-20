import { Utils } from '../core/Utils.js';

export class UIManager {
    static instance = null;

    constructor() {
        this.toastTimeout = null;
    }

    static getInstance() {
        if (!UIManager.instance) {
            UIManager.instance = new UIManager();
        }
        return UIManager.instance;
    }

    showToast(msg, type = 'success', duration = 3000) {
        const toast = document.createElement('div');
        const bgColor = type === 'error' ? 'bg-red-600' : 'bg-gray-900';

        toast.className = `fixed top-4 right-4 ${bgColor} text-white px-6 py-3 rounded-lg shadow-lg animate-fade-in z-50`;
        toast.innerText = msg;

        document.body.appendChild(toast);

        setTimeout(() => {
            toast.classList.add('opacity-0', 'transition-opacity', 'duration-300');
            setTimeout(() => toast.remove(), 300);
        }, duration);
    }

    showLoading(containerId) {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = '<div class="flex items-center justify-center w-full h-screen col-span-full"><div class="spinner">Loading...</div></div>';
        }
    }

    updateUserDisplay(user) {
        const userNameElement = document.getElementById('userName');
        if (userNameElement) {
            userNameElement.innerText = Utils.getUserNameFromEmail(user?.email);
        }
    }

    toggleAuthButtons(isAuthenticated) {
        const loginBtn = document.getElementById('login-btn');
        const logoutBtn = document.getElementById('logout-btn');

        if (isAuthenticated) {
            loginBtn?.classList.add('hidden');
            logoutBtn?.classList.remove('hidden');
        } else {
            loginBtn?.classList.remove('hidden');
            logoutBtn?.classList.add('hidden');
        }
    }

    updateCartBadge(count) {
        const badge = document.getElementById('cart-count');
        if (badge) {
            badge.innerText = count;
            count > 0 ? badge.classList.remove('hidden') : badge.classList.add('hidden');
        }
    }
}