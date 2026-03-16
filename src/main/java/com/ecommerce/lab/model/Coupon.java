package com.ecommerce.lab.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

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

    @Column(name = "discount_percentage", nullable = false)
    private double discountPercentage = 0.0;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "usage_limit", nullable = false)
    private int usageLimit = 0;

    @Column(name = "times_used", nullable = false)
    private int timesUsed = 0;
}