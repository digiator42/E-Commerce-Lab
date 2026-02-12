package com.ecommerce.lab.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CartItemDTO(
        @NotNull(message = "Product ID cannot be null") Long productId,

        @NotBlank(message = "Product name cannot be blank") String productName,

        @Min(value = 1, message = "Quantity must be at least 1") Integer quantity,

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") BigDecimal price) {

}
