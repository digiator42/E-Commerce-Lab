package com.ecommerce.lab.dto;

import java.time.LocalDateTime;

public record ReviewDTO(String userEmail, int rating, String comment, LocalDateTime date) {
}