package com.ecommerce.lab.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank @Size(min = 5, max = 50) String displayName,
        @NotBlank @Size(min = 3, max = 20) String username,
        @NotBlank String address,
        @Min(value = 13) @Max(value = 120) Integer age,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password
) {

}
