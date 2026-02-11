package com.ecommerce.lab.dto;

import com.ecommerce.lab.model.User;

public record UserResponseDTO(
        Long id,
        String name,
        String userName,
        String email) {

    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getUserName(),
                user.getEmail()
            );
    }
}
