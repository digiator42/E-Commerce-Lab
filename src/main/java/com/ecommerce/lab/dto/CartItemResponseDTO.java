package com.ecommerce.lab.dto;

import com.ecommerce.lab.model.CartItem;

public record CartItemResponseDTO(
    Long id,
    Long productId, // Null for Gift Cards
    String name,
    String imageUrl,
    Double price,
    Integer quantity,
    boolean isGiftCard,
    String recipientEmail,
    String message
) {
    public static CartItemResponseDTO fromEntity(CartItem item) {
        if (item.isGiftCard()) {
            return new CartItemResponseDTO(
                item.getId(),
                null,
                "Gift Card ($" + item.getGiftCardAmount() + ")",
                "/images/giftcard-placeholder.png",
                item.getGiftCardAmount(),
                item.getQuantity(),
                true,
                item.getRecipientEmail(),
                item.getGiftCardMessage()
            );
        } else {
            return new CartItemResponseDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getImageUrl(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                false,
                null,
                null
            );
        }
    }
}