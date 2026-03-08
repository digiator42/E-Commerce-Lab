package com.ecommerce.lab.dto;

public record UserUpdateDTO(
        String displayName,
        String userName,
        String currentPassword,
        String newPassword,
        String defaultAddress,
        Double storeBalance
) {

}
