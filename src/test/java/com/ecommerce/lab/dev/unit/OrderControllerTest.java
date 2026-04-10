package com.ecommerce.lab.dev.unit;

import com.ecommerce.lab.dto.OrderRequest;
import com.ecommerce.lab.repository.base.OrderRepository;
import com.ecommerce.lab.dto.GiftCardRequest;
import com.ecommerce.lab.service.OrderService;
import com.ecommerce.lab.service.InvoiceService;
import com.ecommerce.lab.dev.BaseControllerTest;
import com.ecommerce.lab.dev.utils.TestDataFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest extends BaseControllerTest {

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private InvoiceService invoiceService;

    @Test
    @WithMockUser
    @DisplayName("Should place order successfully")
    void shouldPlaceOrder() throws Exception {
        // Given
        List<GiftCardRequest> giftCards = List.of(
            new GiftCardRequest(
                50.0,
                "friend@example.com",
                "Happy Birthday!"
            )
        );

        OrderRequest request = new OrderRequest(
            "",
            true,
            "123 Main St",
            giftCards
        );

        doNothing().when(orderService).placeOrder(
            anyString(), anyString(), anyBoolean(), anyString(), anyList()
        );

        // When/Then
        mockMvc.perform(
            post("/api/orders/place")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Order successfully created"));
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle invalid coupon")
    void shouldHandleInvalidCoupon() throws Exception {
        // Given
        OrderRequest request = new OrderRequest(
            "INVALID",
            false,
            "123 Main St",
            List.of()
        );

        doThrow(new RuntimeException("Invalid coupon code"))
            .when(orderService)
            .placeOrder(anyString(), anyString(), anyBoolean(), anyString(), anyList());

        // When/Then
        mockMvc.perform(
            post("/api/orders/place")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Invalid coupon code"));
    }

    @Test
    @DisplayName("Should reject when not authenticated")
    void shouldReject_WhenNotAuthenticated() throws Exception {
        // Given
        OrderRequest request = new OrderRequest(
            null,
            false,
            "123 Main St",
            List.of()
        );

        // When/Then
        mockMvc.perform(
            post("/api/orders/place")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return user orders")
    void shouldReturnUserOrders() throws Exception {
        // Given
        when(orderRepository.findByUserEmailOrderByOrderDateDesc(anyString()))
            .thenReturn(List.of(TestDataFactory.createTestOrder()));

        // When/Then
        mockMvc.perform(get("/api/orders/my-orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].totalAmount").value(59.98));
    }

    @Test
    @DisplayName("Should return unauthorized when not authenticated")
    void shouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/orders/my-orders"))
            .andExpect(status().isUnauthorized());
    }
}