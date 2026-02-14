package com.ecommerce.lab.dto;

import com.ecommerce.lab.model.User;

public record UserResponseDTO(
        Long id,
        String name,
        String userName,
        String email,
        String role) {

    public static UserResponseDTO fromEntity(User user) {
        String roleName = (user.getRole() != null) ? user.getRole().name() : "ROLE_USER";

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getUserName(),
                user.getEmail(),
                roleName);
    }
}
