package com.ecommerce.lab.dto;

import com.ecommerce.lab.model.CartItem;

public record CartItemResponseDTO(
        Long productId,
        String productName,
        String imageUrl,
        Double price,
        Integer quantity) {
    public static CartItemResponseDTO fromEntity(CartItem item) {
        return new CartItemResponseDTO(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getImageUrl(),
                item.getProduct().getPrice(),
                item.getQuantity());
    }
}
