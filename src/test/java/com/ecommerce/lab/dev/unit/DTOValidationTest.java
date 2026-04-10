package com.ecommerce.lab.dev.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ecommerce.lab.dto.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DTOValidationTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Nested
    @DisplayName("RegisterRequestDTO Validation")
    class RegisterRequestValidation {

        @Test
        @DisplayName("Should pass with valid data")
        void shouldPass_WithValidData() {
            RegisterRequestDTO dto = new RegisterRequestDTO(
                "John Doe",
                "johndoe",
                "123 Main St",
                25,
                "john@example.com",
                "password123"
            );

            Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail with blank display name")
        void shouldFail_WithBlankDisplayName() {
            RegisterRequestDTO dto = new RegisterRequestDTO(
                "",
                "johndoe",
                "123 Main St",
                25,
                "john@example.com",
                "password123"
            );

            Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(2);

            violations.forEach(el -> {
                String msg = el.getMessage();
                if (msg.contains("size")) {
                    assertThat(msg).contains("size must be between 5 and 50");
                } else {
                    assertThat(msg).contains("must not be blank");
                }
            });
        }

        @Test
        @DisplayName("Should fail with short display name")
        void shouldFail_WithShortDisplayName() {
            RegisterRequestDTO dto = new RegisterRequestDTO(
                "Jo",
                "johndoe",
                "123 Main St",
                25,
                "john@example.com",
                "password123"
            );

            Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("size must be between 5 and 50");
        }

        @Test
        @DisplayName("Should fail with invalid email")
        void shouldFail_WithInvalidEmail() {
            RegisterRequestDTO dto = new RegisterRequestDTO(
                "John Doe",
                "johndoe",
                "123 Main St",
                25,
                "invalid-email",
                "password123"
            );

            Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("must be a well-formed email address");
        }

        @Test
        @DisplayName("Should fail with underage user")
        void shouldFail_WithUnderageUser() {
            RegisterRequestDTO dto = new RegisterRequestDTO(
                "John Doe",
                "johndoe",
                "123 Main St",
                12,
                "john@example.com",
                "password123"
            );

            Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("must be greater than or equal to 13");
        }

        @Test
        @DisplayName("Should fail with short password")
        void shouldFail_WithShortPassword() {
            RegisterRequestDTO dto = new RegisterRequestDTO(
                "John Doe",
                "johndoe",
                "123 Main St",
                25,
                "john@example.com",
                "123"
            );

            Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("size must be between 8 and 20");
        }
    }

    @Nested
    @DisplayName("ProductRequestDTO Validation")
    class ProductRequestValidation {

        @Test
        @DisplayName("Should pass with valid data")
        void shouldPass_WithValidData() {
            ProductRequestDTO dto = new ProductRequestDTO(
                "Laptop",
                "High-end gaming laptop",
                50,
                999.99,
                "Electronics"
            );

            Set<ConstraintViolation<ProductRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail with blank name")
        void shouldFail_WithBlankName() {
            ProductRequestDTO dto = new ProductRequestDTO(
                "",
                "Description",
                50,
                999.99,
                "Electronics"
            );

            Set<ConstraintViolation<ProductRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Name is required");
        }

        @Test
        @DisplayName("Should fail with negative stock")
        void shouldFail_WithNegativeStock() {
            ProductRequestDTO dto = new ProductRequestDTO(
                "Laptop",
                "Description",
                -5,
                999.99,
                "Electronics"
            );

            Set<ConstraintViolation<ProductRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Stock cannot be negative");
        }

        @Test
        @DisplayName("Should fail with negative price")
        void shouldFail_WithNegativePrice() {
            ProductRequestDTO dto = new ProductRequestDTO(
                "Laptop",
                "Description",
                50,
                -10.0,
                "Electronics"
            );

            Set<ConstraintViolation<ProductRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Price cannot be negative");
        }

        @Test
        @DisplayName("Should fail with blank category")
        void shouldFail_WithBlankCategory() {
            ProductRequestDTO dto = new ProductRequestDTO(
                "Laptop",
                "Description",
                50,
                999.99,
                ""
            );

            Set<ConstraintViolation<ProductRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("Category is required");
        }
    }

    @Nested
    @DisplayName("GiftCardRequest Validation")
    class GiftCardRequestValidation {

        @Test
        @DisplayName("Should pass with valid data")
        void shouldPass_WithValidData() {
            GiftCardRequest dto = new GiftCardRequest(
                50.0,
                "friend@example.com",
                "Happy Birthday!"
            );

            Set<ConstraintViolation<GiftCardRequest>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail with amount less than 25")
        void shouldFail_WithLowAmount() {
            GiftCardRequest dto = new GiftCardRequest(
                10.0,
                "friend@example.com",
                "Message"
            );

            Set<ConstraintViolation<GiftCardRequest>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Amount must be at least 25");
        }

        @Test
        @DisplayName("Should fail with invalid recipient email")
        void shouldFail_WithInvalidEmail() {
            GiftCardRequest dto = new GiftCardRequest(
                50.0,
                "invalid-email",
                "Message"
            );

            Set<ConstraintViolation<GiftCardRequest>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("must be a well-formed email address");
        }
    }

    @Nested
    @DisplayName("CartItemDTO Validation")
    class CartItemValidation {

        @Test
        @DisplayName("Should pass with valid data")
        void shouldPass_WithValidData() {
            CartItemDTO dto = new CartItemDTO(
                1L,
                "Product",
                2,
                new BigDecimal("29.99")
            );

            Set<ConstraintViolation<CartItemDTO>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail with null product ID")
        void shouldFail_WithNullProductId() {
            CartItemDTO dto = new CartItemDTO(
                null,
                "Product",
                2,
                new BigDecimal("29.99")
            );

            Set<ConstraintViolation<CartItemDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Product ID cannot be null");
        }

        @Test
        @DisplayName("Should fail with quantity less than 1")
        void shouldFail_WithInvalidQuantity() {
            CartItemDTO dto = new CartItemDTO(
                1L,
                "Product",
                0,
                new BigDecimal("29.99")
            );

            Set<ConstraintViolation<CartItemDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Quantity must be at least 1");
        }
    }

    @Nested
    @DisplayName("UpdateUserDTO Validation")
    class UpdateUserDTO {

        @Test
        @DisplayName("Should fail with short displayname")
        void shouldFail_WithShortDisplayName() {
            UserUpdateDTO userUpdateDTO = new UserUpdateDTO(
                "q",
                null,
                null,
                null,
                null,
                null
            );

            Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(userUpdateDTO);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("size must be between 5 and 50");

        }

        @Test
        @DisplayName("Should fail with short userName")
        void shouldFail_WithShortuserName() {
            UserUpdateDTO userUpdateDTO = new UserUpdateDTO(
                null,
                "null",
                null,
                null,
                null,
                null
            );

            Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(userUpdateDTO);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("size must be between 5 and 20");

        }

        @Test
        @DisplayName("Should fail with short defaultAddress")
        void shouldFail_WithShortdefaultAddress() {
            UserUpdateDTO userUpdateDTO = new UserUpdateDTO(
                null,
                null,
                null,
                null,
                "adr",
                null
            );

            Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(userUpdateDTO);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("size must be between 5 and 50");

        }

    }
}