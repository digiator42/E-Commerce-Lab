import { Utils } from '../core/Utils.js';

export class UIManager {
    static instance = null;

    constructor() {
        this.toastTimeout = null;
        this.userMenuOpen = false;
        this.initializeAuthUI();
        this.setupClickOutside();
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
        toast.className = `fixed top-12 right-4 ${bgColor} text-white px-6 py-3 rounded-lg shadow-lg animate-fade-in z-50`;
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

    showSpinner(event, id) {
        event.preventDefault();
        const spinner = document.getElementById(`spinner-${id}`);
        if (spinner) {
            spinner.classList.remove("hidden");
        }

        setTimeout(() => {
            if (spinner) {
                spinner.classList.add("hidden");
            }
        }, 1500);
    }


    updateUserDisplay(user) {
        if (!user) return;

        // Desktop user info
        const userNameDisplay = document.getElementById('user-name-display');
        const userInitials = document.getElementById('user-initials');

        if (userNameDisplay) {
            const displayName = user.displayName || user.email?.split('@')[0] || 'User';
            userNameDisplay.textContent = displayName;
        }

        if (userInitials) {
            const initial = (user.displayName || user.email || 'U').charAt(0).toUpperCase();
            userInitials.textContent = initial;
        }

        // Mobile user info
        const mobileUserName = document.getElementById('mobile-user-name');
        if (mobileUserName) {
            mobileUserName.textContent = user.displayName || user.email?.split('@')[0] || 'User';
        }
    }

    initializeAuthUI() {
        console.log('Initializing authentication UI');
        const user = JSON.parse(localStorage.getItem('user'));
        const isAuthenticated = !!user;

        // Update UI immediately from localStorage
        this.toggleAuthButtons(isAuthenticated);
        this.updateUserDisplay(user);

        // Initialize counts from localStorage
        this.initializeCounts();
    }

    initializeCounts() {
        // You can store cart/wishlist counts in localStorage
        const cartCount = localStorage.getItem('cartCount') || 0;
        const wishlistCount = localStorage.getItem('wishlistCount') || 0;

        this.updateCartBadge(cartCount);
        this.updateWishlistBadge(wishlistCount);
    }

    toggleAuthButtons(isAuthenticated) {
        // Desktop elements
        const desktopAuthButtons = document.getElementById('desktop-auth-buttons');
        const desktopUserMenu = document.getElementById('desktop-user-menu');

        // Mobile elements
        const mobileAuthButtons = document.getElementById('mobile-auth-buttons');
        const mobileUserMenu = document.getElementById('mobile-user-menu');

        if (isAuthenticated) {
            // Hide auth buttons, show user menu
            desktopAuthButtons?.classList.add('hidden');
            desktopUserMenu?.classList.remove('hidden');

            mobileAuthButtons?.classList.add('hidden');
            mobileUserMenu?.classList.remove('hidden');
        } else {
            // Show auth buttons, hide user menu
            desktopAuthButtons?.classList.remove('hidden');
            desktopUserMenu?.classList.add('hidden');

            mobileAuthButtons?.classList.remove('hidden');
            mobileUserMenu?.classList.add('hidden');
        }
    }

    updateCartBadge(count) {
        const desktopBadge = document.getElementById('cart-count-desktop');
        const mobileBadge = document.getElementById('cart-count-mobile');
        const countNum = parseInt(count) || 0;

        [desktopBadge, mobileBadge].forEach(badge => {
            if (badge) {
                badge.textContent = countNum;
                badge.classList.toggle('hidden', countNum === 0);
            }
        });

        // Store in localStorage for immediate display on next page load
        localStorage.setItem('cartCount', countNum);
    }

    updateWishlistBadge(count) {
        const desktopBadge = document.getElementById('wishlist-count-desktop');
        const mobileBadge = document.getElementById('wishlist-count-mobile');

        const countNum = parseInt(count) || 0;

        [desktopBadge, mobileBadge].forEach(badge => {
            if (badge) {
                badge.textContent = countNum;
                badge.classList.toggle('hidden', countNum === 0);
            }
        });

        localStorage.setItem('wishlistCount', countNum);
    }

    toggleUserDropdown() {
        const dropdown = document.getElementById('user-dropdown');
        if (dropdown) {
            this.userMenuOpen = !this.userMenuOpen;
            dropdown.classList.toggle('hidden', !this.userMenuOpen);
        }
    }

    setupClickOutside() {
        document.addEventListener('click', (e) => {
            const button = document.getElementById('user-menu-button');
            const dropdown = document.getElementById('user-dropdown');

            if (this.userMenuOpen && button && !button.contains(e.target) && dropdown && !dropdown.contains(e.target)) {
                this.userMenuOpen = false;
                dropdown.classList.add('hidden');
            }
        });
    }

    // Mobile menu toggle
    toggleMobileMenu() {
        const mobileMenu = document.getElementById('mobile-menu');
        if (mobileMenu) {
            mobileMenu.classList.toggle('hidden');
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