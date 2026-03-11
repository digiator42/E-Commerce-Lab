package com.ecommerce.lab.dto;

import java.math.BigDecimal;

import com.ecommerce.lab.model.OrderItem;

public record OrderItemDTO(
    Long id,
    String productName,
    BigDecimal priceAtPurchase,
    Integer quantity,
    String type, // "PHYSICAL" or "VIRTUAL"
    ProductResponseDTO product,
    GiftCardResponseDTO gc
) {

    public static OrderItemDTO fromEntity(OrderItem oi) {

        String type = (oi.getGiftCard() != null) ? "VIRTUAL" : "PHYSICAL";

        ProductResponseDTO productDTO = oi.getProduct() != null
            ? ProductResponseDTO.simpleFromEntity(oi.getProduct())
            : null;
        GiftCardResponseDTO gcDTO = oi.getGiftCard() != null
            ? GiftCardResponseDTO.fromEntity(oi.getGiftCard())
            : null;

        String name = oi.getGiftCard() != null
            ? "Digital Gift Card (To: " + oi.getGiftCard().getRecipientEmail() + ")"
            : oi.getProduct().getName();

        return new OrderItemDTO(
            oi.getId(),
            name,
            BigDecimal.valueOf(oi.getPriceAtPurchase()),
            oi.getQuantity(),
            type,
            productDTO,
            gcDTO
        );
    }
}
