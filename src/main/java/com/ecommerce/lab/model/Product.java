package com.ecommerce.lab.model;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "products")
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

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    private String brand;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;
}
