package com.ecommerce.lab.utils;

import com.ecommerce.lab.dto.*;
import com.ecommerce.lab.model.*;
import java.time.LocalDateTime;

public class TestDataFactory {

    public static RegisterRequestDTO createValidRegisterRequest() {
        return new RegisterRequestDTO(
            "John Doe",
            "johndoe",
            "123 Main St",
            25,
            "john@example.com",
            "password123"
        );
    }

    public static LoginRequestDTO createValidLoginRequest() {
        return new LoginRequestDTO(
            "john@example.com",
            "password123",
            null
        );
    }

    public static ProductRequestDTO createValidProductRequest() {
        return new ProductRequestDTO(
            "Test Product",
            "Test Description",
            100,
            29.99,
            "Electronics"
        );
    }

    public static User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setUserName("johndoe");
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.ROLE_USER);
        user.setStoreBalance(100.0);
        return user;
    }

    public static Product createTestProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(29.99);
        product.setStock(100);
        product.setAverageRating(4.5);
        product.setTotalReviews(10);

        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        product.setCategory(category);

        return product;
    }

    public static Review createTestReview() {
        Review review = new Review();
        review.setId(1L);
        review.setRating(5);
        review.setComment("Great product!");
        review.setCreatedAt(LocalDateTime.now());
        return review;
    }

    public static Order createTestOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setTotalAmount(59.98);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    public static GiftCard createTestGiftCard() {
        GiftCard giftCard = new GiftCard();
        giftCard.setId(1L);
        giftCard.setCode("TEST-1234-5678");
        giftCard.setInitialAmount(50.0);
        giftCard.setBalance(50.0);
        giftCard.setActive(true);
        giftCard.setRecipientEmail("friend@example.com");
        return giftCard;
    }

    public static ProductResponseDTO createTestProductResponse() {
        return ProductResponseDTO.fromEntity(createTestProduct());
    }

    public static ProductResponseDTO createTestProductResponse(boolean simple) {
        return ProductResponseDTO.simpleFromEntity(createTestProduct());
    }

    public static UserResponseDTO createTestUserResponse() {
        return UserResponseDTO.fromEntity(createTestUser());
    }

    public static OrderResponseDTO createTestOrderResponse() {
        return OrderResponseDTO.fromEntity(createTestOrder());
    }
}
