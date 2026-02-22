export class Product {
    constructor(data = {}) {
        this.id = data.id || null;
        this.name = data.name || '';
        this.description = data.description || '';
        this.price = data.price || 0;
        this.stock = data.stock || 0;
        this.category = data.category || '';
        this.imageUrl = data.imageUrl || null;
        this.reviewStatus = data.reviewStatus || null;
        this.averageRating = data.averageRating || 0;
        this.totalReviews = data.totalReviews || 0;
        this.reviews = data.reviews || [];
    }

    isInStock() {
        return this.stock > 0;
    }

    getFormattedPrice() {
        return `$${this.price.toFixed(2)}`;
    }

    getRatingStars() {
        return '★'.repeat(Math.round(this.averageRating)) + '☆'.repeat(5 - Math.round(this.averageRating));
    }

    matchesPriceRange(min, max) {
        return this.price >= min && this.price <= max;
    }

    matchesRating(minRating) {
        return this.averageRating >= minRating;
    }

    matchesCategory(categories) {
        if (!categories || categories.length === 0) return true;
        return categories.includes(this.category);
    }

    matchesSearch(searchTerm) {
        if (!searchTerm) return true;
        const term = searchTerm.toLowerCase();
        return this.name.toLowerCase().includes(term) ||
            this.description.toLowerCase().includes(term) ||
            this.category.toLowerCase().includes(term);
    }
}