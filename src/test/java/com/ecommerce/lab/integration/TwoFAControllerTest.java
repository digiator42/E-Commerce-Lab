package com.ecommerce.lab.integration;

import com.ecommerce.lab.BaseControllerTest;
import com.ecommerce.lab.dto.LoginRequestDTO;
import com.ecommerce.lab.model.Role;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.base.UserRepository;
import com.ecommerce.lab.utils.TestDataFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @Transactional
class TwoFAControllerTest extends BaseControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.findByEmail("test2fa@example.com")
            .ifPresent(userRepository::delete);
        // Create test user with 2FA disabled initially
        testUser = new User();
        testUser.setEmail("test2fa@example.com");
        testUser.setUserName("test2fa");
        testUser.setName("Test 2FA User");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(Role.ROLE_USER);
        testUser.set2faEnabled(false);
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() { userRepository.delete(testUser); }

    @Test
    @WithMockUser(username = "test2fa@example.com")
    @DisplayName("Should setup TOTP and return QR code URL")
    void shouldSetupTotp() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/2fa/totp/setup"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.qrCodeUrl").exists())
            .andExpect(jsonPath("$.qrCodeUrl").value(containsString("otpauth://totp")));

        // Verify user has TOTP secret saved
        User updatedUser = userRepository.findByEmail("test2fa@example.com").orElseThrow();
        assertThat(updatedUser.getTotpSecret()).isNotNull();
    }

    @Test
    @WithMockUser(username = "test2fa@example.com")
    @DisplayName("Should confirm TOTP with valid code")
    void shouldConfirmTotpWithValidCode() throws Exception {
        // First setup TOTP to get secret
        User user = userRepository.findByEmail("test2fa@example.com").orElseThrow();
        String secret = TestDataFactory.TwoFATestUtil.generateTestSecret();
        user.setTotpSecret(secret);
        userRepository.save(user);

        int validCode = TestDataFactory.TwoFATestUtil.generateValidCode(secret);

        // When/Then
        mockMvc.perform(
            post("/api/2fa/totp/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("code", validCode)))
        )
            .andExpect(status().isOk())
            .andExpect(content().string("Google Authenticator enabled successfully!"));

        // Verify 2FA is enabled
        User updatedUser = userRepository.findByEmail("test2fa@example.com").orElseThrow();
        assertThat(updatedUser.isTotpEnabled()).isTrue();
    }

    @Test
    @WithMockUser(username = "test2fa@example.com")
    @DisplayName("Should reject TOTP confirmation with invalid code")
    void shouldRejectTotpWithInvalidCode() throws Exception {
        // Setup TOTP
        User user = userRepository.findByEmail("test2fa@example.com").orElseThrow();
        String secret = TestDataFactory.TwoFATestUtil.generateTestSecret();
        user.setTotpSecret(secret);
        userRepository.save(user);

        int invalidCode = TestDataFactory.TwoFATestUtil.generateInvalidCode();

        // When/Then
        mockMvc.perform(
            post("/api/2fa/totp/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("code", invalidCode)))
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid code. Please try again."));

        // Verify 2FA is still disabled
        User updatedUser = userRepository.findByEmail("test2fa@example.com").orElseThrow();
        assertThat(updatedUser.isTotpEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should verify 2FA code and complete login")
    void shouldVerify2FACodeAndCompleteLogin() throws Exception {
        // Given
        String secret = TestDataFactory.TwoFATestUtil.generateTestSecret();
        testUser.set2faEnabled(true);
        testUser.setTotpSecret(secret);
        userRepository.save(testUser);

        LoginRequestDTO loginRequest = new LoginRequestDTO(
            "test2fa@example.com",
            "password123",
            null
        );

        var loginResult = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk())
            .andReturn();

        // Extract session from login response to reuse in 2FA verify
        var session = loginResult.getRequest().getSession();

        int validCode = TestDataFactory.TwoFATestUtil.generateValidCode(secret);

        mockMvc.perform(
            post("/api/2fa/verify")
                .session((MockHttpSession) session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("code", String.valueOf(validCode))))
        )
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject login with invalid 2FA code")
    void shouldRejectLoginWithInvalid2FACode() throws Exception {
        // Given - Enable 2FA for user
        testUser.set2faEnabled(true);
        testUser.setTotpSecret(TestDataFactory.TwoFATestUtil.generateTestSecret());
        userRepository.save(testUser);

        // First, login to get session
        LoginRequestDTO loginRequest = new LoginRequestDTO(
            "test2fa@example.com",
            "password123",
            null
        );

        var mocMvcResult = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk());

        var session = mocMvcResult.andReturn().getRequest().getSession();

        // Then verify with invalid code
        int invalidCode = TestDataFactory.TwoFATestUtil.generateInvalidCode();

        mockMvc.perform(
            post("/api/2fa/verify")
                .session((MockHttpSession) session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(Map.of("code", String.valueOf(invalidCode)))
                )
        )
            .andExpect(status().isUnauthorized())
            .andExpect(content().string("Invalid code"));
    }

    @Test
    @WithMockUser(username = "test2fa@example.com")
    @DisplayName("Should enable 2FA")
    void shouldEnable2FA() throws Exception {
        // When
        mockMvc.perform(
            post("/api/2fa/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("enabled", "true")))
        )
            .andExpect(status().isOk())
            .andExpect(content().string("2FA is enabled."));

        // Then
        User updatedUser = userRepository.findByEmail("test2fa@example.com").orElseThrow();
        assertThat(updatedUser.is2faEnabled()).isTrue();
    }

    @Test
    @WithMockUser(username = "test2fa@example.com")
    @DisplayName("Should disable 2FA")
    void shouldDisable2FA() throws Exception {
        // Given - Enable 2FA first
        testUser.set2faEnabled(true);
        userRepository.save(testUser);

        // When
        mockMvc.perform(
            post("/api/2fa/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("enabled", "false")))
        )
            .andExpect(status().isOk())
            .andExpect(content().string("2FA has been disabled."));

        // Then
        User updatedUser = userRepository.findByEmail("test2fa@example.com").orElseThrow();
        assertThat(updatedUser.is2faEnabled()).isFalse();
    }

    @Test
    @WithMockUser(username = "test2fa@example.com")
    @DisplayName("Should get 2FA status")
    void shouldGet2FAStatus() throws Exception {
        // Given
        testUser.set2faEnabled(true);
        userRepository.save(testUser);

        // When/Then
        mockMvc.perform(get("/api/2fa/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("test2fa@example.com"))
            .andExpect(jsonPath("$.is2faEnabled").value(true))
            .andExpect(jsonPath("$.role").exists());
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/2fa/status"))
            .andExpect(status().isUnauthorized());
    }
}