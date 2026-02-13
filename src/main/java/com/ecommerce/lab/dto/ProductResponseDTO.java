package com.ecommerce.lab.dto;

import com.ecommerce.lab.model.Product;

public record ProductResponseDTO(
        Long id,
        String name,
        String description,
        Double price,
        Integer stock,
        String category) {

    public static ProductResponseDTO fromEntity(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory().getName());
    }
}