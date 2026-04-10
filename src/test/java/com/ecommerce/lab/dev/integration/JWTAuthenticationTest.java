package com.ecommerce.lab.dev.integration;

import com.ecommerce.lab.dev.BaseControllerTest;
import com.ecommerce.lab.dev.utils.TestDataFactory;
import com.ecommerce.lab.model.Role;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.base.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("JWT Authentication Tests")
class JWTAuthenticationTest extends BaseControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockHttpSession session;

    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {

        session = new MockHttpSession();

        userRepository.findByEmail("testJwt@example.com")
            .ifPresent(userRepository::delete);

        testUser = new User();
        testUser.setEmail("testJwt@example.com");
        testUser.setUserName("testJwt");
        testUser.setName("Test Jwt User");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(Role.ROLE_USER);
        testUser.set2faEnabled(false);

        // Generate valid token
        validToken = TestDataFactory.JwtTestUtil.generateTestToken(testUser.getEmail());

        testUser.setToken(validToken);
        userRepository.save(testUser);
        userRepository.flush();
    }

    @AfterEach
    void tearDown() {
        userRepository.delete(testUser);
        SecurityContextHolder.clearContext();
        session.invalidate();
    }

    @Test
    @DisplayName("Should authenticate with valid JWT token")
    void shouldAuthenticateWithValidToken() throws Exception {
        mockMvc.perform(
            get("/api/auth/is-logged-in")
                .header("Authorization", "Bearer " + validToken)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(testUser.getEmail()))
            .andExpect(jsonPath("$.userName").value(testUser.getUserName()));
    }

    @Test
    @DisplayName("Should reject request without token")
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/is-logged-in"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject request with invalid token")
    void shouldRejectRequestWithInvalidToken() throws Exception {
        String invalidToken = TestDataFactory.JwtTestUtil.generateInvalidToken();

        mockMvc.perform(
            get("/api/auth/is-logged-in")
                .header("Authorization", "Bearer " + invalidToken)
        )
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject request with expired token")
    void shouldRejectRequestWithExpiredToken() throws Exception {
        String expiredToken = TestDataFactory.JwtTestUtil.generateExpiredToken(testUser.getEmail());

        mockMvc.perform(
            get("/api/auth/is-logged-in")
                .header("Authorization", "Bearer " + expiredToken)
        )
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject request with malformed token")
    void shouldRejectRequestWithMalformedToken() throws Exception {
        mockMvc.perform(
            get("/api/auth/is-logged-in")
                .header("Authorization", "Bearer test.malformed.token")
        )
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should access protected endpoint with valid JWT")
    void shouldAccessProtectedEndpointWithValidJWT() throws Exception {

        mockMvc.perform(
            get("/api/cart")
                .header("Authorization", "Bearer " + validToken)
        )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should access user profile with valid JWT")
    void shouldAccessUserProfileWithValidJWT() throws Exception {
        mockMvc.perform(
            put("/api/users/profile")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "displayName", "Updated Name",
                            "defaultAddress", "New Address"
                        )
                    )
                )
        )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should access orders with valid JWT")
    void shouldAccessOrdersWithValidJWT() throws Exception {
        mockMvc.perform(
            get("/api/orders/my-orders")
                .header("Authorization", "Bearer " + validToken)
        )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should blacklist token on logout")
    void shouldBlacklistTokenOnLogout() throws Exception {

        mockMvc.perform(
            get("/api/auth/is-logged-in")
                .header("Authorization", "Bearer " + validToken)
        )
            .andExpect(status().isOk());

        // Then logout with token
        mockMvc.perform(
            post("/api/auth/logout")
                .header("Authorization", "Bearer " + validToken)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out successfully"));

        // Try to use blacklisted token
        mockMvc.perform(
            get("/api/auth/is-logged-in")
                .header("Authorization", "Bearer " + validToken)
        )
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should clear user token on logout")
    void shouldClearUserTokenOnLogout() throws Exception {
        // Set token for user
        testUser.setToken(validToken);
        userRepository.save(testUser);

        // Logout
        mockMvc.perform(
            post("/api/auth/logout")
                .header("Authorization", "Bearer " + validToken)
        )
            .andExpect(status().isOk());

        // Verify user token is cleared
        User updatedUser = userRepository.findByEmail(testUser.getEmail()).orElseThrow();
        assertThat(updatedUser.getToken()).isNull();
    }

    @Test
    @DisplayName("Should deny admin endpoints to USER role")
    void shouldDenyAdminEndpointsToUserRole() throws Exception {
        mockMvc.perform(
            get("/api/admin/stats")
                .header("Authorization", "Bearer " + validToken)
        )
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow admin endpoints with ADMIN role")
    void shouldAllowAdminEndpointsWithAdminRole() throws Exception {

        userRepository.findByEmail("admin@example.com")
            .ifPresent(userRepository::delete);

        // Create admin user
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setUserName("admin");
        adminUser.setName("Admin User");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRole(Role.ROLE_ADMIN);

        String adminToken = TestDataFactory.JwtTestUtil.generateTestToken(adminUser.getEmail());

        adminUser.setToken(adminToken);
        userRepository.save(adminUser);

        mockMvc.perform(
            get("/api/admin/stats")
                .header("Authorization", "Bearer " + adminToken)
        )
            .andExpect(status().isOk());
    }
}