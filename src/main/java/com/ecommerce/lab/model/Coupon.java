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

    @Column(name = "discount_percentage", nullable = false, columnDefinition = "float(53) default 0.0")
    private double discountPercentage;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    private boolean isActive = true;

    @Column(name = "usage_limit", nullable = false, columnDefinition = "int default 0")
    private int usageLimit;

    @Column(name = "times_used", nullable = false, columnDefinition = "int default 0")
    private int timesUsed = 0;
}