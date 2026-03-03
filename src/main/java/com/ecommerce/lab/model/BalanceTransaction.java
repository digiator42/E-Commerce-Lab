package com.ecommerce.lab.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private double amount; // Positive for redemption, negative for spending
    private String code; // The Gift Card code
    private LocalDateTime date;
    private String type; // "REDEEM" or "PURCHASE"
}