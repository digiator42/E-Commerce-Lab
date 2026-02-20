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
        console.log('Showing toast:', msg, msg.substring(0, 100));
        toast.innerText = msg.length > 100 ? msg.substring(0, 100) + '...' : msg;

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

    async confirm(title, message) {
        return new Promise((resolve) => {
            const dialogHtml = `
            <div id="confirmation-overlay" class="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                <div class="bg-white w-full max-w-sm rounded-3xl p-6 shadow-2xl scale-in-center">
                    <h3 class="text-xl font-bold mb-4">${title}</h3>
                    <p class="text-gray-600 mb-6">${message}</p>
                    <div class="flex justify-end gap-4">
                        <button id="confirm-no" class="px-4 py-2 rounded-lg bg-gray-200 font-bold">No</button>
                        <button id="confirm-yes" class="px-4 py-2 rounded-lg bg-blue-600 text-white font-bold">Yes</button>
                    </div>
                </div>
            </div>
            `;
            document.body.insertAdjacentHTML('beforeend', dialogHtml);

            const confirmYes = document.getElementById('confirm-yes');
            const confirmNo = document.getElementById('confirm-no');

            confirmYes.addEventListener('click', () => {
                document.getElementById('confirmation-overlay').remove();
                resolve(true);
            });

            confirmNo.addEventListener('click', () => {
                document.getElementById('confirmation-overlay').remove();
                resolve(false);
            });
        });
    }
}