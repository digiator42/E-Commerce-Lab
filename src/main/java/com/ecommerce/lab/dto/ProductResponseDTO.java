package com.ecommerce.lab.dto;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;

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
        List<ReviewDTO> reviews
) {

        public static ProductResponseDTO fromEntity(Product product) {
                return fromEntity(product, null);
        }

        public static ProductResponseDTO fromEntity(Product product, String reviewStatus) {
                // 1. Safe check for Category
                String categoryName = Hibernate.isInitialized(product.getCategory())
                        ? product.getCategory().getName()
                        : "Unknown";

                // 2. Safe check for Reviews
                List<ReviewDTO> reviewDTOs = new ArrayList<>();
                double average = 0.0;
                int reviewCount = 0;

                if (Hibernate.isInitialized(product.getReviews())) {
                        reviewCount = product.getReviews().size();
                        average = product.getReviews().stream()
                                .mapToInt(Review::getRating).average().orElse(0.0);

                        reviewDTOs = product.getReviews().stream().map(r -> {
                                // 3. Safe check for Review User
                                String email = Hibernate.isInitialized(r.getUser())
                                        ? r.getUser().getEmail()
                                        : "Anonymous";
                                return new ReviewDTO(
                                        email, r.getRating(), r.getComment(), r.getCreatedAt()
                                );
                        }).toList();
                }

                return new ProductResponseDTO(
                        product.getId(), product.getName(), product.getDescription(),
                        product.getPrice(), product.getStock(), categoryName,
                        product.getImageUrl(), reviewStatus, average, reviewCount, reviewDTOs
                );
        }

        public static ProductResponseDTO simpleFromEntity(Product product) {

                return new ProductResponseDTO(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getStock(),
                        product.getCategory().getName(),
                        product.getImageUrl(),
                        null,
                        null,
                        null,
                        null
                );
        }

}
