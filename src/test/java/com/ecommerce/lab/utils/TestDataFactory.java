package com.ecommerce.lab.utils;

import com.ecommerce.lab.dto.*;
import com.ecommerce.lab.model.*;
import com.warrenstrange.googleauth.GoogleAuthenticator;

import io.jsonwebtoken.Jwts;

import java.time.LocalDateTime;
import java.util.HashMap;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

public class TestDataFactory {

    public static RegisterRequestDTO createValidRegisterRequest() {
        return new RegisterRequestDTO(
            "John Doe",
            "johndoe",
            "123 Main St",
            25,
            "john@example.com",
            "Password&123!"
        );
    }

    public static RegisterRequestDTO createInvalidEmailRegisterRequest() {
        return new RegisterRequestDTO(
            "John Doe",
            "johndoe",
            "123 main St",
            25,
            "john@example",
            "Password&123!"

        );
    }

    public static RegisterRequestDTO createInvalidPasswordRegisterRequest() {
        return new RegisterRequestDTO(
            "John Doe",
            "johndoe",
            "123 main St",
            25,
            "john@example.com",
            "weak"

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
        user.setPassword("password123");
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

    @Component
    public static class JwtTestUtil {

        private static String jwtSecret;

        @Value("${spring.security.jwt-key}")
        public void setJwtSecret(String secret) { JwtTestUtil.jwtSecret = secret; }

        public static String getJwtSecret() { return jwtSecret; }

        public static String generateTestToken(String email) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", email);

            return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
                .signWith(SignatureAlgorithm.HS512, getJwtSecret())
                .compact();
        }

        public static String generateExpiredToken(String email) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", email);

            return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis() - 86400000)) // 1 day ago
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .signWith(SignatureAlgorithm.HS512, getJwtSecret())
                .compact();
        }

        public static String generateInvalidToken() { return "invalid.token.here"; }
    }

    @Component
    public static class TwoFATestUtil {

        private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();

        public static String generateTestSecret() { return gAuth.createCredentials().getKey(); }

        public static int generateValidCode(String secret) { return gAuth.getTotpPassword(secret); }

        public static int generateInvalidCode() {
            return 123456; // Invalid code
        }

        public static String generateTestQrUrl(String email, String secret) {
            return "otpauth://totp/ECommerce:" + email + "?secret=" + secret + "&issuer=ECommerce";
        }
    }
}
