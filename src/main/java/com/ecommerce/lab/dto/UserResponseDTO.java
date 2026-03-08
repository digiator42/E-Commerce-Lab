package com.ecommerce.lab.dto;

import java.time.LocalDateTime;

import com.ecommerce.lab.model.User;

public record UserResponseDTO(
        Long id,
        String displayName,
        String userName,
        String email,
        String profilePicture,
        String defaultAddress,
        String lastLogin,
        String role
) {

        public static UserResponseDTO fromEntity(User user) {
                String roleName = (user.getRole() != null) ? user.getRole().name() : "ROLE_USER";

                String userLastLogin = user.getLastLogin() != null ? user.getLastLogin().toString()
                        : LocalDateTime.now().toString();

                return new UserResponseDTO(
                        user.getId(),
                        user.getName(),
                        user.getUserName(),
                        user.getEmail(),
                        user.getProfilePicture(),
                        user.getAddress(),
                        userLastLogin,
                        roleName
                );
        }
}
