package com.ecommerce.lab.dto;

import java.math.BigDecimal;

import com.ecommerce.lab.model.OrderItem;

public record OrderItemDTO(
    Long id,
    String productName,
    BigDecimal priceAtPurchase,
    Integer quantity,
    ProductResponseDTO product
) {

    public static OrderItemDTO fromEntity(OrderItem oi) {

        return new OrderItemDTO(
            oi.getId(),
            oi.getProductName(),
            BigDecimal.valueOf(oi.getPriceAtPurchase()),
            oi.getQuantity(),
            ProductResponseDTO.simpleFromEntity(oi.getProduct())
        );
    }
}
