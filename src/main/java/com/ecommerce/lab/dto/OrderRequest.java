package com.ecommerce.lab.dto;

public record OrderRequest(
        String couponCode,
        boolean useStoreBalance,
        String shippingAddress) {
}
