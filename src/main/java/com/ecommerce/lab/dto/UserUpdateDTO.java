package com.ecommerce.lab.dto;

public record UserUpdateDTO(
        String name,
        String userName,
        String currentPassword,
        String newPassword) {
}