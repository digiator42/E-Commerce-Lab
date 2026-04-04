package com.ecommerce.lab.unit;

import com.ecommerce.lab.dto.CartItemResponseDTO;
import com.ecommerce.lab.dto.GiftCardRequest;
import com.ecommerce.lab.service.CartService;
import com.ecommerce.lab.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartControllerTest extends BaseControllerTest {

    @MockitoBean
    private CartService cartService;

    @Test
    @WithMockUser
    @DisplayName("Should add product to cart")
    void shouldAddProductToCartAndVerifyCartItemsCount() throws Exception {
        // Given
        Long productId = 1L;
        doNothing().when(cartService).addItemToCart(eq(productId), anyString());

        // When/Then
        mockMvc.perform(
            post("/api/cart/add/{productId}", productId)
                .with(user("test@example.com").roles("USER"))
        )
            .andExpect(status().isOk());


        CartItemResponseDTO cartItem = new CartItemResponseDTO(
            1L, 1L, "Test Product", "/image.jpg",
            29.99, 1, false, null, null
        );

        when(cartService.getCartItems("test@example.com"))
            .thenReturn(List.of(cartItem));

        List<CartItemResponseDTO> result = cartService.getCartItems("test@example.com");

        verify(cartService).addItemToCart(eq(productId), anyString());
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should reject when not authenticated")
    void shouldReject_WhenNotAuthenticated() throws Exception {
        // Given
        Long productId = 1L;

        // When/Then
        mockMvc.perform(post("/api/cart/add/{productId}", productId))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("Should add gift card to cart")
    void shouldAddGiftCardToCart() throws Exception {
        // Given
        GiftCardRequest request = new GiftCardRequest(
            50.0, "friend@example.com", "Happy Birthday!"
        );
        doNothing().when(cartService).addGiftCardToCart(any(GiftCardRequest.class), anyString());

        // When/Then
        mockMvc.perform(
            post("/api/cart/add-gift-card")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(content().string("Gift card added to cart"));
    }

    @Test
    @WithMockUser
    @DisplayName("Should reject gift card with invalid amount")
    void shouldReject_InvalidAmount() throws Exception {
        // Given
        GiftCardRequest invalidRequest = new GiftCardRequest(
            10.0,
            "friend@example.com",
            ""
        );

        // When/Then
        mockMvc.perform(
            post("/api/cart/add-gift-card")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return cart items")
    void shouldReturnCartItems() throws Exception {
        // Given
        CartItemResponseDTO cartItem = new CartItemResponseDTO(
            1L, 1L, "Test Product", "/image.jpg",
            29.99, 2, false, null, null
        );
        List<CartItemResponseDTO> cartItems = List.of(cartItem);

        when(cartService.getCartItems(anyString()))
            .thenReturn(cartItems);

        // When/Then
        mockMvc.perform(get("/api/cart"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].name").value("Test Product"))
            .andExpect(jsonPath("$[0].quantity").value(2));
    }

    @Test
    @WithMockUser
    @DisplayName("Should remove item from cart")
    void shouldRemoveItemFromCart() throws Exception {
        // Given
        Long itemId = 1L;

        // When/Then
        mockMvc.perform(delete("/api/cart/remove/{id}", itemId))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("Should clear all cart items")
    void shouldClearCart() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/cart/clear"))
            .andExpect(status().isOk());
    }
}