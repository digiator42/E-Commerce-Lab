package com.ecommerce.lab.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String productName; // Snapshot of the name
    private Double priceAtPurchase; // Snapshot of the price
    private Integer quantity;
}