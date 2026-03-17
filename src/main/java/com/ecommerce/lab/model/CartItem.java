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

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "is_gift_card", nullable = false)
    private boolean isGiftCard = false;

    @Column(name = "gift_card_amount", nullable = false)
    private double giftCardAmount = 0.0;
    
    private String recipientEmail;
    
    private String giftCardMessage;

    private Integer quantity;
}