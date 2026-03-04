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

    showLoading(containerId, type) {

        if (type === undefined || type == null) {
            type = document.title.split("|")[1].trim().toLocaleLowerCase();
            console.log("===> ", type);
        }

        const container = document.getElementById(containerId);
        if (!container) return;

        switch (true) {
            case type === 'products':
                container.innerHTML = this.getProductSkeleton();
                break;
            case /^product\/\d+$/.test(type):
                console.log("------->> ")
                container.innerHTML = this.getProductDetailSkeleton();
                break;
            case type === 'orders':
                container.innerHTML = this.getOrdersSkeleton();
                break;
            case type === 'checkout':
                container.innerHTML = this.getCheckoutSkeleton();
                break;
            case type === 'profile':
                container.innerHTML = this.getProfileSkeleton();
                break;
            default:
                container.innerHTML = this.getDefaultSkeleton();
        }
    }

    getProductSkeleton() {
        return Array(12).fill(0).map(() => `
            <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden animate-pulse">
                <div class="h-56 bg-gray-200"></div>
                <div class="p-6">
                    <div class="flex items-center justify-between mb-2">
                        <div class="h-6 bg-gray-200 rounded w-2/3"></div>
                        <div class="h-4 bg-gray-200 rounded w-12"></div>
                    </div>
                    <div class="h-4 bg-gray-200 rounded w-full mb-2"></div>
                    <div class="h-4 bg-gray-200 rounded w-5/6 mb-6"></div>
                    <div class="flex justify-between items-center">
                        <div>
                            <div class="h-3 bg-gray-200 rounded w-12 mb-2"></div>
                            <div class="h-6 bg-gray-200 rounded w-20"></div>
                            <div class="h-5 bg-gray-200 rounded-full w-16 mt-2"></div>
                        </div>
                        <div class="w-12 h-12 bg-gray-200 rounded-xl"></div>
                    </div>
                </div>
            </div>
        `).join('');
    }


    getProductDetailSkeleton() {
        return `
            <div class="h-6 bg-gray-200 rounded w-32 mb-8"></div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-12">
                <div class="aspect-square bg-gray-200 rounded-3xl"></div>
                <div class="space-y-6">
                    <div class="h-6 bg-gray-200 rounded w-24"></div>
                    <div class="h-12 bg-gray-200 rounded w-3/4"></div>
                    <div class="h-8 bg-gray-200 rounded w-32"></div>
                    <div class="h-6 bg-gray-200 rounded w-40"></div>
                    <div class="space-y-3">
                        <div class="h-4 bg-gray-200 rounded w-full"></div>
                        <div class="h-4 bg-gray-200 rounded w-5/6"></div>
                        <div class="h-4 bg-gray-200 rounded w-4/6"></div>
                    </div>
                    <div class="h-14 bg-gray-200 rounded-2xl w-full"></div>
                </div>
            </div>
    `;
    }

    getOrdersSkeleton() {
        return `
        <div class="max-w-3xl mx-auto space-y-6 animate-pulse">
            <!-- Header Skeleton -->
            <div class="flex items-center space-x-4 p-4  mb-10">
                <div class="w-14 h-14 bg-gray-200 rounded-2xl"></div>
                <div>
                    <div class="h-8 bg-gray-200 rounded w-48 mb-2"></div>
                    <div class="h-4 bg-gray-200 rounded w-64"></div>
                </div>
            </div>

            <!-- Simplified Order Cards -->
            ${Array(3).fill(0).map(() => `
                <div class="bg-white rounded-2xl border border-gray-100 p-6">
                    <!-- Header -->
                    <div class="flex justify-between items-center border-b pb-4 mb-4">
                        <div>
                            <div class="h-4 bg-gray-200 rounded w-24 mb-2"></div>
                            <div class="h-6 bg-gray-200 rounded w-32"></div>
                        </div>
                        <div class="h-6 bg-gray-200 rounded w-28"></div>
                    </div>

                    <!-- Items -->
                    <div class="space-y-3">
                        ${Array(2).fill(0).map(() => `
                            <div class="flex justify-between">
                                <div class="h-4 bg-gray-200 rounded w-2/3"></div>
                                <div class="h-4 bg-gray-200 rounded w-16"></div>
                            </div>
                        `).join('')}
                    </div>

                    <!-- Footer -->
                    <div class="border-t mt-4 pt-4 flex justify-between">
                        <div class="h-5 bg-gray-200 rounded w-20"></div>
                        <div class="h-8 bg-gray-200 rounded w-32"></div>
                    </div>
                </div>
            `).join('')}
        </div>
    `;
    }

    getCheckoutSkeleton() {
        return `
        <div class="max-w-7xl mx-auto py-8 px-4">
            <div class="flex justify-center mb-8 animate-pulse">
                <div class="flex items-center space-x-4">
                    <div class="w-8 h-8 bg-gray-200 rounded-full"></div>
                    <div class="w-12 h-0.5 bg-gray-200"></div>
                    <div class="w-8 h-8 bg-gray-200 rounded-full"></div>
                    <div class="w-12 h-0.5 bg-gray-200"></div>
                    <div class="w-8 h-8 bg-gray-200 rounded-full"></div>
                </div>
            </div>
            
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 animate-pulse">
                <div class="lg:col-span-2 space-y-6">
                    <div class="bg-white rounded-2xl p-6">
                        <div class="h-6 bg-gray-200 rounded w-48 mb-4"></div>
                        <div class="h-12 bg-gray-200 rounded w-full"></div>
                    </div>
                    <div class="bg-white rounded-2xl p-6">
                        <div class="h-6 bg-gray-200 rounded w-48 mb-4"></div>
                        <div class="grid grid-cols-2 gap-4">
                            <div class="h-12 bg-gray-200 rounded col-span-2"></div>
                            <div class="h-12 bg-gray-200 rounded"></div>
                            <div class="h-12 bg-gray-200 rounded"></div>
                            <div class="h-12 bg-gray-200 rounded"></div>
                            <div class="h-12 bg-gray-200 rounded"></div>
                        </div>
                    </div>
                </div>
                <div class="lg:col-span-1">
                    <div class="bg-white rounded-2xl p-6 sticky top-24">
                        <div class="h-6 bg-gray-200 rounded w-32 mb-4"></div>
                        <div class="space-y-3 mb-4">
                            ${Array(3).fill(0).map(() => `
                                <div class="flex justify-between">
                                    <div class="h-4 bg-gray-200 rounded w-2/3"></div>
                                    <div class="h-4 bg-gray-200 rounded w-16"></div>
                                </div>
                            `).join('')}
                        </div>
                        <div class="h-12 bg-gray-200 rounded w-full mt-6"></div>
                    </div>
                </div>
            </div>
        </div>
    `;
    }

    getProfileSkeleton() {
        return `
        <div class="max-w-4xl mx-auto animate-pulse">
            <div class="mb-8">
                <div class="h-8 bg-gray-200 rounded w-48 mb-2"></div>
                <div class="h-4 bg-gray-200 rounded w-64"></div>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div class="bg-white rounded-2xl p-6">
                    <div class="flex flex-col items-center">
                        <div class="w-24 h-24 bg-gray-200 rounded-full mb-4"></div>
                        <div class="h-6 bg-gray-200 rounded w-32 mb-2"></div>
                        <div class="h-4 bg-gray-200 rounded w-48"></div>
                    </div>
                </div>
                <div class="md:col-span-2 bg-white rounded-2xl p-6">
                    <div class="h-6 bg-gray-200 rounded w-48 mb-6"></div>
                    <div class="space-y-4">
                        <div class="h-12 bg-gray-200 rounded w-full"></div>
                        <div class="h-12 bg-gray-200 rounded w-full"></div>
                        <div class="h-12 bg-gray-200 rounded w-1/2"></div>
                    </div>
                </div>
            </div>
        </div>
    `;
    }

    getDefaultSkeleton() {
        return `
        <div class="flex items-center justify-center w-full h-64">
            <div class="spinner"></div>
        </div>
    `;
    }

    // Also add a shimmer effect CSS
    addShimmerStyles() {
        const style = document.createElement('style');
        style.textContent = `
        @keyframes shimmer {
            0% {
                background-position: -1000px 0;
            }
            100% {
                background-position: 1000px 0;
            }
        }
        
        .animate-pulse {
            background: linear-gradient(to right, #f0f0f0 8%, #f8f8f8 18%, #f0f0f0 33%);
            background-size: 1000px 100%;
            animation: shimmer 2s infinite linear;
        }
    `;
        document.head.appendChild(style);
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