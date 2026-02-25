package com.ecommerce.lab.dto;

import com.ecommerce.lab.model.User;

public record UserResponseDTO(
        Long id,
        String displayName,
        String userName,
        String email,
        String profilePicture,
        String defaultAddress,
        String lastLogin,
        String role) {

    public static UserResponseDTO fromEntity(User user) {
        String roleName = (user.getRole() != null) ? user.getRole().name() : "ROLE_USER";

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getUserName(),
                user.getEmail(),
                user.getProfilePicture(),
                user.getAddress(),
                user.getLastLogin().toString(),
                roleName);
    }
}
