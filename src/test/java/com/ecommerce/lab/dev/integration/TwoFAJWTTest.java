package com.ecommerce.lab.dev.integration;

import com.ecommerce.lab.dev.BaseControllerTest;
import com.ecommerce.lab.dev.utils.TestDataFactory;
import com.ecommerce.lab.dto.LoginRequestDTO;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("2FA + JWT Integration Tests")
class TwoFAJWTTest extends BaseControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockHttpSession session;
    private User testUser;
    private String secret;

    @BeforeEach
    void setUp() {

        session = new MockHttpSession();

        userRepository.findByEmail("integrate@example.com")
            .ifPresent(userRepository::delete);

        // Create test user with 2FA enabled
        secret = TestDataFactory.TwoFATestUtil.generateTestSecret();
        testUser = new User();
        testUser.setEmail("integrate@example.com");
        testUser.setUserName("integrate");
        testUser.setName("Integration Test User");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(Role.ROLE_USER);
        testUser.setTotpEnabled(true);
        testUser.set2faEnabled(true);
        testUser.setTotpSecret(secret);
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.delete(testUser);
        session.invalidate();
    }

    @Test
    @DisplayName("Complete flow: Login with 2FA -> Get JWT -> Access protected endpoints")
    void completeLoginFlowWith2FAAndJWT() throws Exception {
        // Login with credentials
        LoginRequestDTO loginRequest = new LoginRequestDTO(
            testUser.getEmail(),
            "password123",
            null
        );

        mockMvc.perform(
            post("/api/auth/login")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requires2FA").value(true))
            .andExpect(jsonPath("$.isFirstTimeSetup").value(false));

        // Verify 2FA code
        int validCode = TestDataFactory.TwoFATestUtil.generateValidCode(secret);

        var returnedUser = mockMvc.perform(
            post("/api/2fa/verify")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("code", String.valueOf(validCode))))
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        // Extract response body as String
        String responseBody = returnedUser.getResponse().getContentAsString();
        // Parse JSON to get the token
        String userToken = objectMapper.readTree(responseBody).get("token").asText();

        mockMvc.perform(
            get("/api/cart")
                .header("Authorization", "Bearer " + userToken)
        )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should maintain 2FA session across JWT requests")
    void shouldMaintain2FASessionAcrossJWTRequests() throws Exception {
        // Login and verify 2FA
        LoginRequestDTO loginRequest = new LoginRequestDTO(
            testUser.getEmail(),
            "password123",
            null
        );

        mockMvc.perform(
            post("/api/auth/login")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk());

        int validCode = TestDataFactory.TwoFATestUtil.generateValidCode(secret);

        mockMvc.perform(
            post("/api/2fa/verify")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("code", String.valueOf(validCode))))
        )
            .andExpect(status().isOk());

        // Get user token
        User updatedUser = userRepository.findByEmail(testUser.getEmail()).orElseThrow();
        String userToken = updatedUser.getToken();

        // Multiple requests should work with same token
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(
                get("/api/cart")
                    .header("Authorization", "Bearer " + userToken)
            )
                .andExpect(status().isOk());
        }
    }
}