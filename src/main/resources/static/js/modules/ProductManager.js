import { ComponentStore } from '../core/ComponentStore.js';
import { UIManager } from './UIManager.js';
import { CartManager } from './CartManager.js';

export class ProductManager {
    static instance = null;

    constructor() {
        this.currentPage = 0;
        this.currentSearch = '';
        this.currentCategory = null;
        this.selectedRating = 0;
        this.totalPages = 0;
        this.componentStore = ComponentStore.getInstance();
        this.uiManager = UIManager.getInstance();
        this.cartManager = CartManager.getInstance();
        this.searchDebounce = null;
    }

    static getInstance(apiClient) {
        if (!ProductManager.instance) {
            ProductManager.instance = new ProductManager();
            ProductManager.instance.apiClient = apiClient;
        }
        return ProductManager.instance;
    }

    async fetchProducts(sortBy = null) {
        let url = `/api/products?page=${this.currentPage}&size=6`;
        if (this.currentSearch) url += `&search=${encodeURIComponent(this.currentSearch)}`;
        if (this.currentCategory) url += `&category=${encodeURIComponent(this.currentCategory)}`;
        if (sortBy) url += `&sort=${sortBy}`;

        window.history.pushState(null, '', url.substring(5));

        try {
            const data = await this.apiClient.fetch(url);
            this.totalPages = data.totalPages;
            return data;
        } catch (error) {
            console.error('Error fetching products:', error);
            return null;
        }
    }

    async renderProducts(containerId, sortBy = null) {
        const container = document.getElementById(containerId);
        const data = await this.fetchProducts(sortBy);

        if (!data) return;

        // Update pagination
        document.getElementById('prev-btn').disabled = data.first;
        document.getElementById('next-btn').disabled = data.last;
        document.getElementById('page-info').innerText = `Page ${data.number + 1} of ${data.totalPages}`;

        const cardTemplate = await this.componentStore.load('product-card');

        container.innerHTML = data.content.map(p => {
            const imageSrc = p.imageUrl || 'https://placehold.co/600x400/EEE/31343C';
            return cardTemplate
                .replace(/{{imageSrc}}/g, imageSrc)
                .replace(/{{name}}/g, p.name)
                .replace(/{{description}}/g, p.description)
                .replace(/{{price}}/g, p.price.toFixed(2))
                .replace(/{{id}}/g, p.id)
                .replace(/{{category}}/g, p.category);
        }).join('');
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
        this.renderProducts('product-list-container');
    }
    
    filterProducts() {
        const filterValue = document.getElementById('products-filter').value;
        this.renderProducts('product-list-container', filterValue);
    }

    handleSearch(event) {
        this.currentSearch = event.target.value;
        this.currentPage = 0;

        this.uiManager.showLoading('product-list-container');

        clearTimeout(this.searchDebounce);
        this.searchDebounce = setTimeout(() => {
            this.renderProducts('product-list-container');
        }, 300);
    }

    changePage(direction) {
        this.currentPage += direction;
        this.renderProducts('product-list-container');
    }
}