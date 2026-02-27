import { ComponentStore } from '../core/ComponentStore.js';
import { UIManager } from './UIManager.js';
import { CartManager } from './CartManager.js';
import { WishlistManager } from './WishlistManager.js';
import { Product } from '../models/Product.js';

export class ProductManager {
    static instance = null;

    constructor() {
        this.currentPage = 0;
        this.currentSearch = '';
        this.selectedCategories = new Set();
        this.priceRange = { min: 0, max: 1000 };
        this.minRating = 0;
        this.inStockOnly = false;
        this.sortBy = '';
        this.totalPages = 0;
        this.totalElements = 0;
        this.allProducts = [];
        this.filteredProducts = []; // Store filtered results separately
        this.apiClient = null;
        this.componentStore = ComponentStore.getInstance();
        this.uiManager = UIManager.getInstance();
        this.searchDebounce = null;
        this.cartManager = null;
        this.wishlistManager = null;
    }

    static getInstance(apiClient) {
        if (!ProductManager.instance) {
            ProductManager.instance = new ProductManager();
            ProductManager.instance.apiClient = apiClient;
            ProductManager.instance.cartManager = CartManager.getInstance(apiClient);
            ProductManager.instance.wishlistManager = WishlistManager.getInstance(apiClient);
        }
        return ProductManager.instance;
    }

    // Update URL with current filters
    updateURL() {
        const params = new URLSearchParams();

        if (this.currentSearch) params.set('search', this.currentSearch);
        if (this.sortBy) params.set('sort', this.sortBy);

        // Handle multiple categories
        if (this.selectedCategories.size > 0) {
            params.delete('category');
            Array.from(this.selectedCategories).forEach(category => {
                params.append('category', category);
            });
        }

        // Add minRating if > 0
        if (this.minRating > 0) params.set('minRating', this.minRating);

        // Add price range if API supports it
        if (this.priceRange.min > 0) params.set('minPrice', this.priceRange.min);
        if (this.priceRange.max < 1000) params.set('maxPrice', this.priceRange.max);

        // In stock filter (client-side only for now)
        // if (this.inStockOnly) params.set('inStock', 'true');

        if (this.currentPage > 0) params.set('page', this.currentPage);

        const queryString = params.toString();
        const newUrl = queryString ? `/products?${queryString}` : '/products';

        console.log('URL updated to:', newUrl);
        window.history.pushState({}, '', newUrl);
    }

    loadFiltersFromURL() {
        const params = new URLSearchParams(window.location.search);

        this.currentSearch = params.get('search') || '';
        this.sortBy = params.get('sort') || '';
        this.currentPage = parseInt(params.get('page')) || 0;

        // Load minRating from URL
        this.minRating = parseInt(params.get('minRating')) || 0;

        // Load price range if present
        this.priceRange = {
            min: parseInt(params.get('minPrice')) || 0,
            max: parseInt(params.get('maxPrice')) || 1000
        };

        // In stock is client-side only
        // this.inStockOnly = params.get('inStock') === 'true';

        // Parse multiple categories
        const categories = params.getAll('category');
        if (categories.length > 0) {
            this.selectedCategories = new Set(categories);
        } else {
            this.selectedCategories.clear();
        }

        console.log('Filters loaded from URL:', {
            search: this.currentSearch,
            sort: this.sortBy,
            categories: Array.from(this.selectedCategories),
            priceRange: this.priceRange,
            minRating: this.minRating,
            page: this.currentPage
        });
    }

    // Sync UI elements with current filter state
    syncUIWithFilters() {
        // Update search input
        const searchInput = document.getElementById('product-search');
        if (searchInput) searchInput.value = this.currentSearch;

        // Update sort select
        const sortSelect = document.getElementById('products-filter');
        if (sortSelect) sortSelect.value = this.sortBy;

        // Update category checkboxes
        document.querySelectorAll('.category-checkbox').forEach(cb => {
            if (cb.dataset.category === 'all') {
                cb.checked = this.selectedCategories.size === 0;
            } else {
                cb.checked = this.selectedCategories.has(cb.dataset.category);
            }
        });

        // Update price inputs
        const minRange = document.getElementById('min-price-range');
        const maxRange = document.getElementById('max-price-range');
        const minInput = document.getElementById('min-price-input');
        const maxInput = document.getElementById('max-price-input');

        if (minRange) minRange.value = this.priceRange.min;
        if (maxRange) maxRange.value = this.priceRange.max;
        if (minInput) minInput.value = this.priceRange.min;
        if (maxInput) maxInput.value = this.priceRange.max;

        // Update rating radio
        const ratingRadio = document.querySelector(`input[name="rating-filter"][value="${this.minRating}"]`);
        if (ratingRadio) {
            ratingRadio.checked = true;
        } else {
            document.querySelector('input[name="rating-filter"][value="0"]').checked = true;
        }

        // Update in-stock checkbox
        const stockCheckbox = document.getElementById('in-stock-filter');
        if (stockCheckbox) stockCheckbox.checked = this.inStockOnly;
    }

    async fetchProducts() {
        let url = `/api/products/custom?page=${this.currentPage}&size=12`;

        // PRIORITY 1: Search
        if (this.currentSearch) {
            url += `&search=${encodeURIComponent(this.currentSearch)}`;
        }

        // PRIORITY 2: Multiple categories
        if (this.selectedCategories.size > 0) {
            Array.from(this.selectedCategories).forEach(category => {
                url += `&category=${encodeURIComponent(category)}`;
            });
        }

        // PRIORITY 3: Min Rating
        if (this.minRating > 0) {
            url += `&minRating=${this.minRating}`;
        }

        // PRIORITY 4: Max Price
        if (this.priceRange.max < 1000) {
            url += `&maxPrice=${this.priceRange.max}`;
        }

        // PRIORITY 5: Min Price
        if (this.priceRange.min > 0) {
            url += `&minPrice=${this.priceRange.min}`;
        }

        // PRIORITY 6: Sorting
        if (this.sortBy) {
            url += `&sort=${this.sortBy}`;
        }

        try {
            console.log('Fetching products from:', url);
            const data = await this.apiClient.fetch(url);

            if (!data || !data.content) {
                return {
                    content: [],
                    totalPages: 0,
                    totalElements: 0,
                    number: 0,
                    first: true,
                    last: true,
                    empty: true
                };
            }

            // Convert to Product models
            const products = data.content.map(p => new Product(p));

            // Client side filtering
            const filteredProducts = this.applyClientSideFilters(products);

            console.log('Server page info:', {
                page: data.number,
                totalPages: data.totalPages,
                totalElements: data.totalElements,
                first: data.first,
                last: data.last
            });

            console.log('Client-side filtered on current page:', filteredProducts.length, 'of', products.length);

            // Return with server's pagination info
            return {
                content: filteredProducts,
                totalPages: data.totalPages,
                totalElements: data.totalElements,
                number: data.number,
                first: data.first,
                last: data.last,
                empty: filteredProducts.length === 0,
                size: data.size,
                numberOfElements: filteredProducts.length
            };

        } catch (error) {
            console.error('Error fetching products:', error);
            return {
                content: [],
                totalPages: 0,
                totalElements: 0,
                number: 0,
                first: true,
                last: true,
                empty: true
            };
        }
    }

    applyClientSideFilters(products) {
        console.log('Applying client-side filters:', {
            inStockOnly: this.inStockOnly,
            totalProducts: products.length
        });

        return products.filter(product => {

            if (this.inStockOnly) {
                const inStock = product.stock > 0;
                if (!inStock) {
                    return false;
                }
            }

            return true;
        });
    }

    applyClientFilters(products) {
        console.log('Applying client filters:', {
            categories: Array.from(this.selectedCategories),
            priceRange: this.priceRange,
            minRating: this.minRating,
            inStockOnly: this.inStockOnly,
            totalProducts: products.length
        });

        return products.filter(product => {
            // Log each product for debugging
            console.log('Checking product:', {
                name: product.name,
                category: product.category,
                price: product.price,
                rating: product.averageRating,
                stock: product.stock
            });

            // Category filter
            if (this.selectedCategories.size > 0) {
                const productCategory = product.category ? product.category.toLowerCase() : '';
                const matchesCategory = Array.from(this.selectedCategories).some(cat =>
                    cat.toLowerCase() === productCategory
                );
                if (!matchesCategory) {
                    console.log('Failed category filter');
                    return false;
                }
            }

            // Price range filter
            const productPrice = product.price || 0;
            if (productPrice < this.priceRange.min || productPrice > this.priceRange.max) {
                console.log('Failed price filter');
                return false;
            }

            // Rating filter
            const productRating = product.averageRating || 0;
            if (this.minRating > 0 && productRating < this.minRating) {
                console.log('Failed rating filter');
                return false;
            }

            // In stock filter
            if (this.inStockOnly) {
                const inStock = product.stock > 0;
                if (!inStock) {
                    console.log('Failed stock filter');
                    return false;
                }
            }

            console.log('Product passed all filters');
            return true;
        });
    }

    async renderProducts(containerId = 'product-list-container') {
        const container = document.getElementById(containerId);

        // Show loading state
        if (container) {
            container.innerHTML = '<div class="col-span-full text-center py-10"><div class="spinner mx-auto"></div><p class="mt-4 text-gray-500">Loading products...</p></div>';
        }

        const data = await this.fetchProducts();

        if (!data || !container) return;

        // Update URL with current filters
        this.updateURL();

        // Update results count
        const resultsCount = document.getElementById('results-count');
        if (resultsCount) {
            const hasClientFilters = this.priceRange.min > 0 || this.priceRange.max < 1000 ||
                this.minRating > 0 || this.inStockOnly;

            if (hasClientFilters && data.totalElements > 0) {
                resultsCount.textContent = `Showing ${data.content.length} of ${data.totalElements} products (filtered)`;
            } else {
                resultsCount.textContent = `Found ${data.totalElements} products`;
            }
        }

        // Update pagination - USE SERVER DATA
        const prevBtn = document.getElementById('prev-btn');
        const nextBtn = document.getElementById('next-btn');
        const pageInfo = document.getElementById('page-info');

        if (prevBtn) {
            prevBtn.disabled = data.first;
            console.log('Prev button disabled:', data.first);
        }
        if (nextBtn) {
            nextBtn.disabled = data.last;
            console.log('Next button disabled:', data.last);
        }
        if (pageInfo) {
            pageInfo.innerText = `Page ${data.number + 1} of ${data.totalPages}`;
            console.log('Page info:', data.number + 1, 'of', data.totalPages);
        }

        // If no products after filtering
        if (data.content.length === 0) {
            container.innerHTML = `
            <div class="col-span-full text-center py-16">
                <svg class="w-24 h-24 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                <h3 class="text-xl font-bold text-gray-700 mb-2">No products match your filters</h3>
                <p class="text-gray-500">Try adjusting your price range or rating</p>
                <button onclick="window.productManager.clearAllFilters()" class="mt-4 bg-blue-600 text-white px-6 py-2 rounded-xl hover:bg-blue-700 transition">
                    Clear All Filters
                </button>
            </div>
        `;
            return;
        }

        const cardTemplate = await this.componentStore.load('product-card');


        container.innerHTML = data.content.map(p => {
            const imageSrc = p.imageUrl || 'https://placehold.co/600x400/EEE/31343C';
            const isInWishlist = this.wishlistManager?.isInWishlist(p.id) || false;
            const hasReviewStatus = p.reviewStatus && p.reviewStatus !== '';

            return cardTemplate
                .replace(/{{imageSrc}}/g, imageSrc)
                .replace(/{{name}}/g, p.name)
                .replace(/{{description}}/g, p.description)
                .replace(/{{price}}/g, p.price.toFixed(2))
                .replace(/{{id}}/g, p.id)
                .replace(/{{category}}/g, p.category)
                .replace(/{{rating}}/g, p.getRatingStars())
                .replace(/{{reviewCount}}/g, p.totalReviews)
                .replace(/{{reviewStatus}}/g, p.reviewStatus || '')
                .replace(/{{reviewStatusDisplay}}/g, hasReviewStatus ? 'block' : 'none')
                .replace(/{{wishlistClass}}/g, isInWishlist ? 'text-red-500 fill-current' : 'text-gray-400')
                .replace(/{{wishlistFill}}/g, isInWishlist ? 'currentColor' : 'none');
        }).join('');

        // Update rating counts
        this.updateRatingCounts();

        // Update active filters display
        this.updateActiveFilters();
    }

    // Modified: Search now triggers server fetch
    handleSearch(event) {
        this.currentSearch = event.target.value;
        this.currentPage = 0; // Reset to first page

        clearTimeout(this.searchDebounce);
        this.searchDebounce = setTimeout(() => {
            this.renderProducts();
        }, 300);
    }

    // Modified: Category filter is client-side only
    toggleCategoryFilter(category) {
        console.log('Toggling category:', category);
        console.log('Current selected before:', Array.from(this.selectedCategories));

        if (category === null) {
            this.selectedCategories.clear();
            document.querySelectorAll('.category-checkbox').forEach(cb => {
                if (cb.dataset.category === 'all') {
                    cb.checked = true;
                } else {
                    cb.checked = false;
                }
            });
        } else {
            const allCheckbox = document.querySelector('.category-checkbox[data-category="all"]');
            if (allCheckbox) {
                allCheckbox.checked = false;
            }

            if (this.selectedCategories.has(category)) {
                this.selectedCategories.delete(category);
            } else {
                this.selectedCategories.add(category);
            }

            const checkbox = document.querySelector(`.category-checkbox[data-category="${category}"]`);
            if (checkbox) {
                checkbox.checked = this.selectedCategories.has(category);
            }
        }

        console.log('Current selected after:', Array.from(this.selectedCategories));

        this.currentPage = 0;
        this.renderProducts(); // This will now fetch from server with category param
    }

    clearCategoryFilters() {
        this.selectedCategories.clear();
        document.querySelectorAll('.category-checkbox').forEach(cb => {
            cb.checked = cb.dataset.category === 'all';
        });
        this.currentPage = 0;
        this.renderProducts();
    }

    updatePriceRange() {
        const minRange = document.getElementById('min-price-range');
        const maxRange = document.getElementById('max-price-range');
        const minInput = document.getElementById('min-price-input');
        const maxInput = document.getElementById('max-price-input');

        if (!minRange || !maxRange || !minInput || !maxInput) return;

        // Parse values with fallback
        let minVal = parseInt(minRange.value) || 0;
        let maxVal = parseInt(maxRange.value) || 1000;

        // Ensure values are within bounds
        minVal = Math.max(0, Math.min(minVal, 1000));
        maxVal = Math.max(minVal, Math.min(maxVal, 1000));

        // Update inputs
        minInput.value = minVal;
        maxInput.value = maxVal;
        minRange.value = minVal;
        maxRange.value = maxVal;

        // Ensure min <= max
        if (minVal > maxVal) {
            if (event && event.target.id.includes('min')) {
                maxVal = minVal;
            } else {
                minVal = maxVal;
            }
            maxRange.value = maxVal;
            maxInput.value = maxVal;
            minRange.value = minVal;
            minInput.value = minVal;
        }

        this.priceRange = {
            min: minVal,
            max: maxVal
        };

        this.currentPage = 0;
        this.renderProducts();
    }

    setRatingFilter(rating) {
        this.minRating = parseInt(rating) || 0;
        this.currentPage = 0;
        console.log('Setting rating filter to:', this.minRating);
        this.renderProducts();
    }

    toggleInStock() {
        const checkbox = document.getElementById('in-stock-filter');
        if (checkbox) {
            this.inStockOnly = checkbox.checked;
            this.currentPage = 0;
            this.renderProducts();
        }
    }

    applySorting(sortValue) {
        this.sortBy = sortValue;
        this.currentPage = 0;
        this.renderProducts(); // This will trigger server fetch with new sort
    }

    changePage(direction) {
        const newPage = this.currentPage + direction;

        // Ensure we don't go below 0
        if (newPage < 0) return;

        this.currentPage = newPage;
        console.log('Changing to page:', this.currentPage);
        this.renderProducts();
    }

    updateActiveFilters() {
        const container = document.getElementById('active-filters-container');
        const activeFilters = document.getElementById('active-filters');

        if (!container || !activeFilters) return;

        const filters = [];

        if (this.selectedCategories.size > 0) {
            filters.push(...Array.from(this.selectedCategories).map(cat => ({
                type: 'category',
                value: cat,
                label: `Category: ${cat}`
            })));
        }

        if (this.priceRange.min > 0 || this.priceRange.max < 1000) {
            filters.push({
                type: 'price',
                value: `${this.priceRange.min}-${this.priceRange.max}`,
                label: `$${this.priceRange.min} - $${this.priceRange.max}`
            });
        }

        if (this.minRating > 0) {
            filters.push({
                type: 'rating',
                value: this.minRating,
                label: `${this.minRating}+ Stars`
            });
        }

        if (this.inStockOnly) {
            filters.push({
                type: 'stock',
                value: 'in-stock',
                label: 'In Stock Only'
            });
        }

        if (filters.length > 0) {
            container.style.display = 'block';
            activeFilters.innerHTML = filters.map(filter => `
                <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-blue-50 text-blue-700">
                    ${filter.label}
                    <button onclick="window.productManager.removeFilter('${filter.type}', '${filter.value}')" 
                        class="ml-2 text-blue-400 hover:text-blue-600">
                        <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                        </svg>
                    </button>
                </span>
            `).join('');
        } else {
            container.style.display = 'none';
        }
    }

    removeFilter(type, value) {
        switch (type) {
            case 'category':
                this.selectedCategories.delete(value);
                const checkbox = document.querySelector(`.category-checkbox[data-category="${value}"]`);
                if (checkbox) checkbox.checked = false;

                // Check if no categories selected, then check "All Products"
                if (this.selectedCategories.size === 0) {
                    const allCheckbox = document.querySelector('.category-checkbox[data-category="all"]');
                    if (allCheckbox) allCheckbox.checked = true;
                }
                break;
            case 'price':
                this.priceRange = { min: 0, max: 1000 };
                const minRange = document.getElementById('min-price-range');
                const maxRange = document.getElementById('max-price-range');
                const minInput = document.getElementById('min-price-input');
                const maxInput = document.getElementById('max-price-input');

                if (minRange) minRange.value = 0;
                if (maxRange) maxRange.value = 1000;
                if (minInput) minInput.value = 0;
                if (maxInput) maxInput.value = 1000;
                break;
            case 'rating':
                this.minRating = 0;
                const allRatingRadio = document.querySelector('input[name="rating-filter"][value="0"]');
                if (allRatingRadio) allRatingRadio.checked = true;
                break;
            case 'stock':
                this.inStockOnly = false;
                const stockCheckbox = document.getElementById('in-stock-filter');
                if (stockCheckbox) stockCheckbox.checked = false;
                break;
        }

        this.currentPage = 0;
        this.renderProducts();
    }

    clearAllFilters() {
        this.selectedCategories.clear();
        this.priceRange = { min: 0, max: 1000 };
        this.minRating = 0;
        this.inStockOnly = false;
        this.currentSearch = '';
        this.sortBy = '';
        this.currentPage = 0;

        // Reset UI elements
        document.querySelectorAll('.category-checkbox').forEach(cb => {
            cb.checked = cb.dataset.category === 'all';
        });

        const minRange = document.getElementById('min-price-range');
        const maxRange = document.getElementById('max-price-range');
        const minInput = document.getElementById('min-price-input');
        const maxInput = document.getElementById('max-price-input');

        if (minRange) minRange.value = 0;
        if (maxRange) maxRange.value = 1000;
        if (minInput) minInput.value = 0;
        if (maxInput) maxInput.value = 1000;

        const allRatingRadio = document.querySelector('input[name="rating-filter"][value="0"]');
        if (allRatingRadio) allRatingRadio.checked = true;

        const stockCheckbox = document.getElementById('in-stock-filter');
        if (stockCheckbox) stockCheckbox.checked = false;

        const searchInput = document.getElementById('product-search');
        if (searchInput) searchInput.value = '';

        const sortSelect = document.getElementById('products-filter');
        if (sortSelect) sortSelect.value = '';

        this.renderProducts();
    }

    updateRatingCounts() {
        const counts = { 4: 0, 3: 0, 2: 0, 1: 0 };

        this.allProducts.forEach(p => {
            const rating = Math.floor(p.averageRating || 0);
            if (rating >= 4) counts[4]++;
            else if (rating >= 3) counts[3]++;
            else if (rating >= 2) counts[2]++;
            else if (rating >= 1) counts[1]++;
        });

        [4, 3, 2, 1].forEach(stars => {
            const element = document.getElementById(`rating-count-${stars}`);
            if (element) {
                element.textContent = `(${counts[stars]})`;
            }
        });
    }

    setRating(n) {
        this.selectedRating = n;
        const stars = document.querySelectorAll('#star-rating-input button');
        stars.forEach((s, i) => {
            s.style.color = i < n ? '#FBBF24' : '#D1D5DB';
        });
    }

    async submitReview(productId) {
        const comment = document.getElementById('review-comment').value;
        if (this.selectedRating === 0) {
            alert('Please select a star rating');
            return;
        }

        try {
            await this.apiClient.fetch(`/api/reviews/${productId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ rating: this.selectedRating, comment })
            });
            this.uiManager.showToast('Review submitted!');
            window.location.href = `/product/${productId}`;
        } catch (error) {
            console.error('Error submitting review:', error);
        }
    }

    filterCategory(categoryName) {
        this.currentCategory = categoryName;
        this.currentPage = 0;

        document.querySelectorAll('#category-list button').forEach(btn => {
            btn.classList.remove('bg-blue-50', 'text-blue-600', 'font-bold');
        });
        if (event) event.target.classList.add('bg-blue-50', 'text-blue-600', 'font-bold');

        this.renderProducts('product-list-container');
    }

    async loadCategoryProducts(category, containerId) {
        try {
            const response = await fetch(`/api/products/custom?category=${category}&page=0&size=4`);
            const data = await response.json();

            const container = document.getElementById(containerId);
            if (!container) return;

            if (!data.content || data.content.length === 0) {
                container.innerHTML = '<p class="col-span-full text-center text-gray-500">No products found</p>';
                return;
            }

            const cardTemplate = await ComponentStore.getInstance().load('product-card');

            container.innerHTML = data.content.map(p => {
                const imageSrc = p.imageUrl || 'https://placehold.co/600x400/EEE/31343C';
                const isInWishlist = window.wishlistManager?.isInWishlist(p.id) || false;

                return cardTemplate
                    .replace(/{{imageSrc}}/g, imageSrc)
                    .replace(/{{name}}/g, p.name)
                    .replace(/{{description}}/g, p.description.substring(0, 60) + '...')
                    .replace(/{{price}}/g, p.price.toFixed(2))
                    .replace(/{{id}}/g, p.id)
                    .replace(/{{category}}/g, p.category)
                    .replace(/{{rating}}/g, '★★★★☆') // You can add actual rating
                    .replace(/{{reviewCount}}/g, p.reviews?.length || 0)
                    .replace(/{{reviewStatus}}/g, '')
                    .replace(/{{reviewStatusTag}}/g, '')
                    .replace(/{{wishlistClass}}/g, isInWishlist ? 'text-red-500 fill-current' : 'text-gray-400')
                    .replace(/{{wishlistFill}}/g, isInWishlist ? 'currentColor' : 'none');
            }).join('');

        } catch (error) {
            console.error(`Error loading ${category} products:`, error);
            const container = document.getElementById(containerId);
            if (container) {
                container.innerHTML = '<p class="col-span-full text-center text-red-500">Failed to load products</p>';
            }
        }
    }
}