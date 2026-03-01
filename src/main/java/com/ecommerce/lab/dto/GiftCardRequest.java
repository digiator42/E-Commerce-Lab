package com.ecommerce.lab.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;

public record GiftCardRequest(
        @Min(value = 25, message = "Amount must be at least 1") Double amount,
        @Email String recipientEmail) {

}
