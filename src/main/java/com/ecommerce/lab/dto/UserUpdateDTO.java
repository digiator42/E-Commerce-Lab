package com.ecommerce.lab.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateDTO(
        @Size(min = 5, max = 50) String displayName,
        @Size(min = 5, max = 20) String userName,
        String currentPassword,
        String newPassword,
        @Size(min = 5, max = 500) String defaultAddress,
        Double storeBalance
) {

}
