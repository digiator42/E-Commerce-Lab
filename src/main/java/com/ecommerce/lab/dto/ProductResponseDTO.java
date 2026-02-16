package com.ecommerce.lab.dto;

import java.util.List;

import com.ecommerce.lab.model.Product;

public record ProductResponseDTO(
        Long id,
        String name,
        String description,
        Double price,
        Integer stock,
        String category,
        String imageUrl,
        List<ReviewDTO> reviews) {

    public static ProductResponseDTO fromEntity(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory().getName(),
                product.getImageUrl(),
                product.getReviews().stream()
                        .map(r -> new ReviewDTO(r.getUser().getEmail(), r.getRating(), r.getComment(),
                                r.getCreatedAt()))
                        .toList());
    }
}