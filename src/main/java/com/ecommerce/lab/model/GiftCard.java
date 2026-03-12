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
    private String code;

    private String name;

    private double balance; // Remaining money
    private double initialAmount; // Starting money for history/logs

    private LocalDateTime expiryDate;
    private boolean isActive = true;

    // link it to a specific user
    private String recipientEmail;

    private String message;
}