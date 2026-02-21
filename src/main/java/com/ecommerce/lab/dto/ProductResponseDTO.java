package com.ecommerce.lab.dto;

import java.util.List;

import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.model.Review;

public record ProductResponseDTO(
                Long id,
                String name,
                String description,
                Double price,
                Integer stock,
                String category,
                String imageUrl,
                String reviewStatus,
                Double averageRating,
                Integer totalReviews,
                List<ReviewDTO> reviews) {

        public static ProductResponseDTO fromEntity(Product product) {
                return fromEntity(product, null);
        }

        public static ProductResponseDTO fromEntity(Product product, String reviewStatus) {
                double average = product.getReviews().isEmpty() ? 0.0
                                : product.getReviews().stream()
                                                .mapToInt(Review::getRating)
                                                .average()
                                                .orElse(0.0);

                List<ReviewDTO> reviews = product.getReviews()
                                .stream()
                                .map(r -> new ReviewDTO(
                                                r.getUser().getEmail(),
                                                r.getRating(),
                                                r.getComment(),
                                                r.getCreatedAt()))
                                .toList();
                return new ProductResponseDTO(
                                product.getId(),
                                product.getName(),
                                product.getDescription(),
                                product.getPrice(),
                                product.getStock(),
                                product.getCategory().getName(),
                                product.getImageUrl(),
                                reviewStatus,
                                average,
                                product.getReviews().size(),
                                reviews);
        }
}