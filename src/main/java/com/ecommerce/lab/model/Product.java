package com.ecommerce.lab.model;

import java.util.List;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Formula;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "products")
@NamedEntityGraph(name = "Product.fullDetails", attributeNodes = {
                @NamedAttributeNode("category"),
                @NamedAttributeNode(value = "reviews", subgraph = "review-user-subgraph")
}, subgraphs = {
                @NamedSubgraph(name = "review-user-subgraph", attributeNodes = @NamedAttributeNode("user"))
})
@Data
@Transactional
public class Product {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        private Long id;

        @Column(nullable = false)
        private String name;

        @Column(length = 500)
        private String description;

        @Column(nullable = false)
        private Double price;

        @Column(nullable = false)
        private Integer stock;

        @Column(nullable = false)
        private boolean active = true;

        @ManyToOne
        @JoinColumn(name = "category_id")
        private Category category;

        @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
        @BatchSize(size = 10)
        private List<Review> reviews;

        @Formula("(SELECT COALESCE(AVG(r.rating), 0) FROM review r WHERE r.product_id = id)")
        private Double averageRating;

        @Formula("(SELECT COUNT(r.id) FROM review r WHERE r.product_id = id)")
        private Integer totalReviews;

        private String brand;

        @Column(columnDefinition = "TEXT")
        private String imageUrl;
}
