package com.ecommerce.lab.model;

import java.time.LocalDateTime;
import java.util.List;


import java.util.ArrayList;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@NamedEntityGraph(name = "Order.fullDetails", attributeNodes = {
        @NamedAttributeNode(value = "items", subgraph = "items-subgraph"),
        @NamedAttributeNode("user")
}, subgraphs = {
        @NamedSubgraph(name = "items-subgraph", attributeNodes = {
                @NamedAttributeNode(value = "product", subgraph = "product-subgraph")
        }),
        @NamedSubgraph(name = "product-subgraph", attributeNodes = {
                @NamedAttributeNode("category")
        })
})
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime orderDate;
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private String paymentTransactionId;
    private String paymentStatus;

    private String shippingAddress; // Snapshot of address
}