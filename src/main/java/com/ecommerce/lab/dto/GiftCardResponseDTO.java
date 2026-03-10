package com.ecommerce.lab.dto;

import com.ecommerce.lab.model.GiftCard;

public record GiftCardResponseDTO(
    Long id,
    Double amount,
    String recipientEmail,
    String imageUrl
) {
    public static GiftCardResponseDTO fromEntity(GiftCard gc) {
        if (gc == null)
            return null;
        return new GiftCardResponseDTO(
            gc.getId(),
            gc.getInitialAmount(),
            gc.getRecipientEmail(),
            "https://placehold.co/600x400/9333ea/ffffff?text=Gift+Card+"
                + (int) gc.getInitialAmount()
        );
    }
}