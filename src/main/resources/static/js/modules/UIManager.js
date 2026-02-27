import { Utils } from '../core/Utils.js';

export class UIManager {
    static instance = null;

    constructor() {
        this.toastTimeout = null;
        this.userMenuOpen = false;
        this.activeToasts = [];
        this.initializeAuthUI();
        this.setupClickOutside();
    }

    static getInstance() {
        if (!UIManager.instance) {
            UIManager.instance = new UIManager();
        }
        return UIManager.instance;
    }


    showToast(msg, type = 'success', duration = 3000, position = 'top-right') {
        const toast = document.createElement('div');

        // Color mapping
        const colors = {
            success: 'bg-green-600',
            error: 'bg-red-600',
            warning: 'bg-yellow-600',
            info: 'bg-blue-600',
            default: 'bg-gray-900'
        };

        // Position mapping
        const positions = {
            'top-right': 'top-16 right-4',
            'top-left': 'top-16 left-4',
            'bottom-right': 'bottom-4 right-4',
            'bottom-left': 'bottom-4 left-4',
            'top-center': 'top-16 left-1/2 -translate-x-1/2',
            'bottom-center': 'bottom-4 left-1/2 -translate-x-1/2'
        };

        const bgColor = colors[type] || colors.default;
        const positionClass = positions[position] || positions['top-right'];

        // Calculate offset for stacked toasts
        const existingToasts = this.activeToasts.filter(t => t.position === position);
        const offset = existingToasts.length * 70; // 70px offset per toast

        // Create toast container
        toast.className = `fixed ${positionClass} ${bgColor} text-white rounded-lg shadow-lg animate-fade-in z-50 overflow-hidden`;
        toast.style.minWidth = '320px';
        toast.style.marginTop = `${offset}px`;
        toast.style.transition = 'margin-top 0.3s ease';

        // Store toast data
        const toastData = {
            element: toast,
            position: position,
            timeoutId: null
        };

        // Create content container with icon
        const contentDiv = document.createElement('div');
        contentDiv.className = 'flex items-center px-6 py-3';

        // Add icon based on type
        const iconSpan = document.createElement('span');
        iconSpan.className = 'mr-3 text-lg';

        const icons = {
            success: '✓',
            error: '✗',
            warning: '⚠',
            info: 'ℹ',
            default: '•'
        };
        iconSpan.textContent = icons[type] || icons.default;

        // Add message
        const messageSpan = document.createElement('span');
        messageSpan.className = 'flex-1';
        messageSpan.innerText = msg.length > 100 ? msg.substring(0, 100) + '...' : msg;

        // Add close button
        const closeBtn = document.createElement('button');
        closeBtn.className = 'ml-3 text-white/80 hover:text-white focus:outline-none';
        closeBtn.innerHTML = '✕';
        closeBtn.onclick = () => this.removeToast(toast, toastData);

        contentDiv.appendChild(iconSpan);
        contentDiv.appendChild(messageSpan);
        contentDiv.appendChild(closeBtn);

        // Create progress bar container
        const progressContainer = document.createElement('div');
        progressContainer.className = 'h-1 bg-white/30 w-full';

        // Create progress bar
        const progressBar = document.createElement('div');
        progressBar.className = 'h-full bg-white/90 transition-all duration-100 ease-linear';
        progressBar.style.width = '100%';
        progressBar.style.transition = `width ${duration}ms linear`;

        // Assemble toast
        progressContainer.appendChild(progressBar);
        toast.appendChild(contentDiv);
        toast.appendChild(progressContainer);

        document.body.appendChild(toast);
        this.activeToasts.push(toastData);

        // Trigger progress bar animation
        setTimeout(() => {
            progressBar.style.width = '0%';
        }, 10);

        // Set timeout for auto-removal
        const timeoutId = setTimeout(() => {
            this.removeToast(toast, toastData);
        }, duration);

        toastData.timeoutId = timeoutId;

        // Pause progress bar on hover
        toast.addEventListener('mouseenter', () => {
            progressBar.style.transition = 'none';
            progressBar.style.width = progressBar.style.width;
            clearTimeout(timeoutId);
        });

        toast.addEventListener('mouseleave', () => {
            const remainingWidth = parseFloat(progressBar.style.width);
            const remainingTime = (remainingWidth / 100) * duration;

            progressBar.style.transition = `width ${remainingTime}ms linear`;
            progressBar.style.width = '0%';

            const newTimeoutId = setTimeout(() => {
                this.removeToast(toast, toastData);
            }, remainingTime);

            toastData.timeoutId = newTimeoutId;
        });
    }

    removeToast(toast, toastData) {
        // Clear timeout
        if (toastData.timeoutId) {
            clearTimeout(toastData.timeoutId);
        }

        // Remove from active toasts
        const index = this.activeToasts.indexOf(toastData);
        if (index > -1) {
            this.activeToasts.splice(index, 1);
        }

        // Animate out
        toast.classList.add('opacity-0', 'transition-opacity', 'duration-300');

        // Update positions of remaining toasts
        setTimeout(() => {
            toast.remove();
            this.updateToastPositions(toastData.position);
        }, 300);
    }

    updateToastPositions(position) {
        const positionToasts = this.activeToasts
            .filter(t => t.position === position)
            .sort((a, b) => {
                // Sort by creation time (assuming they're in order)
                return this.activeToasts.indexOf(a) - this.activeToasts.indexOf(b);
            });

        positionToasts.forEach((toastData, index) => {
            toastData.element.style.marginTop = `${index * 70}px`;
        });
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