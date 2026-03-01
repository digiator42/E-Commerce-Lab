package com.ecommerce.lab.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class GiftCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // "GIFT-ABCD-1234"

    private double balance; // Remaining money
    private double initialAmount; // Starting money for history/logs

    private LocalDateTime expiryDate;
    private boolean isActive = true;

    // link it to a specific user or (anyone can use)
    private String recipientEmail;

    private String message;
}