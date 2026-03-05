export class Product {
    constructor(data = {}) {
        // Core product fields
        this.id = data.id || null;
        this.name = data.name || 'Unknown Product';
        this.description = data.description || '';
        this.price = data.price || 0;
        this.stock = data.stock || 0;
        this.category = data.category || '';
        this.imageUrl = data.imageUrl || null;

        // Cart-specific fields
        this.cartItemId = data.cartItemId || data.id || null;
        this.quantity = data.quantity || 1;
        this.priceAtPurchase = data.priceAtPurchase || data.price || 0;

        // Review fields
        this.reviewStatus = data.reviewStatus || null;
        this.averageRating = data.averageRating || 0;
        this.totalReviews = data.totalReviews || 0;
        this.reviews = data.reviews || [];

        // Gift card specific fields
        this.isGiftCard = data.isGiftCard === true; // Ensure boolean
        this.recipientEmail = data.recipientEmail || null;
        this.message = data.message || '';

        // Product type detection (will be set after all properties are initialized)
        this.isVirtual = this.isGiftCard;
    }

    // Check if product is in stock (for physical products)
    isInStock() {
        if (this.isVirtual) return true; // Virtual products are always "in stock"
        return this.stock > 0;
    }

    // Get display price (with formatting)
    getFormattedPrice() {
        return `$${this.price.toFixed(2)}`;
    }

    // Get price at purchase (for cart items)
    getEffectivePrice() {
        return this.priceAtPurchase || this.price;
    }

    // Get total price for quantity
    getTotalPrice() {
        return (this.getEffectivePrice() * this.quantity).toFixed(2);
    }

    // Get rating stars as string
    getRatingStars() {
        return '★'.repeat(Math.round(this.averageRating)) + '☆'.repeat(5 - Math.round(this.averageRating));
    }

    // Check if product has reviews
    hasReviews() {
        return this.totalReviews > 0;
    }

    // Get product type badge
    getTypeBadge() {
        return this.isVirtual ? '🎧 Digital' : '📦 Physical';
    }

    // Convert to cart item format
    toCartItem() {
        return {
            id: this.cartItemId,
            productId: this.id,
            name: this.name,
            price: this.getEffectivePrice(),
            quantity: this.quantity,
            imageUrl: this.imageUrl,
            isVirtual: this.isVirtual,
            totalPrice: this.getTotalPrice()
        };
    }

    static fromCartItem(cartItem) {
        return new Product({
            id: cartItem.productId || cartItem.id,
            cartItemId: cartItem.id,
            name: cartItem.name || 'Unknown Product',
            price: cartItem.price || 0,
            quantity: cartItem.quantity || 1,
            imageUrl: cartItem.imageUrl || null,
            isGiftCard: cartItem.isGiftCard === true,
            recipientEmail: cartItem.recipientEmail,
            message: cartItem.message,
            // For gift cards, set appropriate properties
            stock: cartItem.isGiftCard ? -1 : null,
            category: cartItem.isGiftCard ? 'Gift Card' : cartItem.category
        });
    }

    // Create from product API response
    static fromProductResponse(data) {
        return new Product({
            id: data.id,
            name: data.name,
            description: data.description,
            price: data.price,
            stock: data.stock,
            category: data.category?.name || data.category,
            imageUrl: data.imageUrl,
            reviewStatus: data.reviewStatus,
            averageRating: data.averageRating,
            totalReviews: data.totalReviews,
            reviews: data.reviews || []
        });
    }

    // Create from localStorage item
    static fromLocalStorage(item) {
        return new Product({
            id: item.productId,
            cartItemId: item.id,
            name: item.name,
            price: item.price,
            quantity: item.quantity,
            imageUrl: item.imageUrl,
            category: item.category,
            stock: item.stock,
            isGiftCard: item.isGiftCard === true, // Important: ensure boolean
            recipientEmail: item.recipientEmail,
            message: item.message
        });
    }
}