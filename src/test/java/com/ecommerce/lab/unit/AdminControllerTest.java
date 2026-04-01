package com.ecommerce.lab.unit;

import com.ecommerce.lab.model.*;
import com.ecommerce.lab.repository.base.CouponRepository;
import com.ecommerce.lab.repository.base.OrderRepository;
import com.ecommerce.lab.repository.base.ProductRepository;
import com.ecommerce.lab.repository.base.UserRepository;
import com.ecommerce.lab.service.CustomUserDetailsService;
import com.ecommerce.lab.BaseControllerTest;
import com.ecommerce.lab.filter.JwtAuthenticationFilter;
import com.ecommerce.lab.utils.TestDataFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest extends BaseControllerTest {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private CouponRepository couponRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private static final String ADMIN_EMAIL = "admin@test.com";

    private String adminToken;

    @BeforeEach
    void setUp() {

        UserDetails adminDetails = org.springframework.security.core.userdetails.User
            .withUsername(ADMIN_EMAIL)
            .password("password")
            .roles("ADMIN")
            .build();

        when(customUserDetailsService.loadUserByUsername(ADMIN_EMAIL))
            .thenReturn(adminDetails);

        adminToken = TestDataFactory.JwtTestUtil.generateTestToken(ADMIN_EMAIL);
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    @DisplayName("Should return dashboard statistics")
    void shouldReturnDashboardStats() throws Exception {
        // Given
        when(productRepository.count()).thenReturn(12L);
        when(orderRepository.count()).thenReturn(5L);
        when(orderRepository.findAll()).thenReturn(List.of(TestDataFactory.createTestOrder()));
        when(couponRepository.findAll()).thenReturn(List.of());

        // When/Then
        mockMvc.perform(
            get("/api/admin/stats")
                .header("Authorization", "Bearer " + adminToken)
        )

            .andExpect(status().isOk())
            .andExpect(jsonPath("$.products").value(12))
            .andExpect(jsonPath("$.orders").value(5))
            .andExpect(jsonPath("$.revenue").exists())
            .andExpect(jsonPath("$.coupons").isEmpty());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "USER")
    @DisplayName("Should reject non-admin users")
    void shouldRejectNonAdmin() throws Exception {
        // When/Then
        mockMvc.perform(
            get("/api/admin/stats")
                .header("Authorization", "Bearer " + adminToken)
        )
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    @DisplayName("Should return all orders")
    void shouldReturnAllOrders() throws Exception {
        // Given
        Order order = TestDataFactory.createTestOrder();
        when(orderRepository.findAll(any(Sort.class)))
            .thenReturn(List.of(order));

        // When/Then
        mockMvc.perform(
            get("/api/admin/orders")
                .header("Authorization", "Bearer " + adminToken)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].totalAmount").value(59.98));
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    @DisplayName("Should return all users")
    void shouldReturnAllUsers() throws Exception {
        // Given
        User user = TestDataFactory.createTestUser();
        when(userRepository.findAll()).thenReturn(List.of(user));

        // When/Then
        mockMvc.perform(
            get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].email").value("john@example.com"));
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    @DisplayName("Should create coupon")
    void shouldCreateCoupon() throws Exception {
        // Given
        Coupon coupon = new Coupon();
        coupon.setCode("SAVE20");
        coupon.setDiscountPercentage(20.0);

        when(couponRepository.existsByCode(anyString())).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        // When/Then
        mockMvc.perform(
            post("/api/admin/coupons")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(coupon))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("SAVE20"));
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    @DisplayName("Should reject duplicate coupon code")
    void shouldRejectDuplicateCoupon() throws Exception {
        // Given
        Coupon coupon = new Coupon();
        coupon.setCode("SAVE20");
        coupon.setDiscountPercentage(20.0);

        when(couponRepository.existsByCode(anyString())).thenReturn(true);

        // When/Then
        mockMvc.perform(
            post("/api/admin/coupons")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(coupon))
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    @DisplayName("Should update coupon")
    void shouldUpdateCoupon() throws Exception {
        // Given
        Long couponId = 1L;
        Coupon coupon = new Coupon();
        coupon.setCode("SAVE30");
        coupon.setDiscountPercentage(30.0);

        when(couponRepository.findById(eq(couponId))).thenReturn(Optional.of(new Coupon()));
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        // When/Then
        mockMvc.perform(
            put("/api/admin/coupons/{id}", couponId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(coupon))
        )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    @DisplayName("Should delete coupon")
    void shouldDeleteCoupon() throws Exception {
        // Given
        Long couponId = 1L;
        doNothing().when(couponRepository).deleteById(eq(couponId));

        // When/Then
        mockMvc.perform(
            delete("/api/admin/coupons/{id}", couponId)
                .header("Authorization", "Bearer " + adminToken)
        )
            .andExpect(status().isOk());
    }
}