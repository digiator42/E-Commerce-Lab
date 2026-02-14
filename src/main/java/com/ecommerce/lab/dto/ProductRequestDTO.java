package com.ecommerce.lab.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductRequestDTO(
        @NotBlank(message = "Name is required") String name,
        @Size(max = 500, message = "Description cannot be more than 500 chars") String description,
        @Min(value = 0, message = "Stock cannot be negative") Integer stock,
        @Min(value = 1, message = "Price cannot be negative") Double price,
        @NotBlank(message = "Category is required") String categoryName
    ) {
}