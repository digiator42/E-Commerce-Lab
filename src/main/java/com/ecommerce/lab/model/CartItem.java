package com.ecommerce.lab.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cart_items")
@Data
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "is_gift_card", nullable = false, columnDefinition = "boolean default false")
    private boolean isGiftCard = false;
    @Column(name = "gift_card_amount", nullable = false, columnDefinition = "float(53) default 0.0")
    private double giftCardAmount = 0.0;
    private String recipientEmail;
    private String giftCardMessage;

    private Integer quantity;
}