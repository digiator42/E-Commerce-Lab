package com.ecommerce.lab.unit;

import com.ecommerce.lab.model.Review;
import com.ecommerce.lab.repository.base.ProductRepository;
import com.ecommerce.lab.repository.base.ReviewRepository;
import com.ecommerce.lab.repository.base.UserRepository;
import com.ecommerce.lab.service.OrderService;
import com.ecommerce.lab.BaseControllerTest;
import com.ecommerce.lab.utils.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

class ReviewControllerTest extends BaseControllerTest {

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private ReviewRepository reviewRepository;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @WithMockUser
    @DisplayName("Should add review for purchased product")
    void shouldAddReview_ForPurchasedProduct() throws Exception {
        // Given
        Long productId = 1L;
        Review review = TestDataFactory.createTestReview();

        when(orderService.hasUserPurchasedProduct(anyString(), eq(productId)))
            .thenReturn(true);
        when(reviewRepository.existsByUserEmailAndProductId(anyString(), eq(productId)))
            .thenReturn(false);
        when(productRepository.findById(eq(productId)))
            .thenReturn(Optional.of(TestDataFactory.createTestProduct()));
        when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(TestDataFactory.createTestUser()));
        when(reviewRepository.save(any(Review.class)))
            .thenReturn(review);

        // When/Then
        mockMvc.perform(
            post("/api/reviews/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(review))
        )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("Should reject review for unpurchased product")
    void shouldReject_ForUnpurchasedProduct() throws Exception {
        // Given
        Long productId = 1L;
        Review review = TestDataFactory.createTestReview();

        when(orderService.hasUserPurchasedProduct(anyString(), eq(productId)))
            .thenReturn(false);

        // When/Then
        mockMvc.perform(
            post("/api/reviews/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(review))
        )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.error").value("You can only review products you have purchased.")
            );
    }

    @Test
    @WithMockUser
    @DisplayName("Should reject duplicate review")
    void shouldReject_DuplicateReview() throws Exception {
        // Given
        Long productId = 1L;
        Review review = TestDataFactory.createTestReview();

        when(orderService.hasUserPurchasedProduct(anyString(), eq(productId)))
            .thenReturn(true);
        when(reviewRepository.existsByUserEmailAndProductId(anyString(), eq(productId)))
            .thenReturn(true);

        // When/Then
        mockMvc.perform(
            post("/api/reviews/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(review))
        )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("You have already reviewed this product."));
    }

    @Test
    @DisplayName("Should reject when not authenticated")
    void shouldReject_WhenNotAuthenticated() throws Exception {
        // Given
        Long productId = 1L;
        Review review = TestDataFactory.createTestReview();

        // When/Then
        mockMvc.perform(
            post("/api/reviews/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(review))
        )
            .andExpect(status().isUnauthorized());
    }
}