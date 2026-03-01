package com.ecommerce.lab.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private double discountPercentage;
    
    private LocalDateTime expiryDate;
    
    private boolean isActive = true;

    private int usageLimit;
    private int timesUsed = 0;
}