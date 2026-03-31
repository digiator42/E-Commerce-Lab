package com.ecommerce.lab.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.ecommerce.lab.BaseControllerTest;
import com.ecommerce.lab.dto.LoginRequestDTO;
import com.ecommerce.lab.dto.RegisterRequestDTO;
import com.ecommerce.lab.dto.UserResponseDTO;
import com.ecommerce.lab.filter.JwtUtils;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.base.UserRepository;
import com.ecommerce.lab.service.AuthService;
import com.ecommerce.lab.service.CustomUserDetailsService;
import com.ecommerce.lab.service.UserService;
import com.ecommerce.lab.utils.TestDataFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthControllerTest extends BaseControllerTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtUtils jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should register user with valid data")
    void shouldRegisterUser_WithValidData() throws Exception {
        // Given
        RegisterRequestDTO request = TestDataFactory.createValidRegisterRequest();
        UserResponseDTO response = TestDataFactory.createTestUserResponse();

        when(userService.registerUser(any(RegisterRequestDTO.class)))
            .thenReturn(response);

        // When/Then
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("john@example.com"))
            .andExpect(jsonPath("$.displayName").value("John Doe"));
    }

    @Test
    @DisplayName("Should reject user with invalid weak password (criteria)")
    void shouldRejectUser_WithInvalidPassword() throws Exception {
        RegisterRequestDTO request = TestDataFactory.createInvalidPasswordRegisterRequest();
        UserResponseDTO response = TestDataFactory.createTestUserResponse();

        when(userService.registerUser(any(request.getClass()))).thenReturn(response);

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject user with invalid email")
    void shouldRejectUser_WithInvalidEmail() throws Exception {
        RegisterRequestDTO request = TestDataFactory.createInvalidEmailRegisterRequest();
        UserResponseDTO response = TestDataFactory.createTestUserResponse();

        when(userService.registerUser(any(request.getClass()))).thenReturn(response);

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with short password")
    void shouldReject_ShortPassword() throws Exception {
        // Given
        RegisterRequestDTO invalidRequest = new RegisterRequestDTO(
            "John Doe",
            "johndoe",
            "123 Main St",
            25,
            "john@example.com",
            "123" // Password too short
        );

        // When/Then
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should login with valid credentials")
    void shouldLogin_WithValidCredentials() throws Exception {
        // Given
        LoginRequestDTO request = TestDataFactory.createValidLoginRequest();
        UserResponseDTO response = TestDataFactory.createTestUserResponse();
        User mockUser = TestDataFactory.createTestUser();

        when(userService.findByEmail(any(String.class)))
            .thenReturn(mockUser);

        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        when(
            authService.finalizeSession(
                any(User.class),
                any(LoginRequestDTO.class),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
            )
        ).thenReturn(response);

        // When/Then
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userName").value("johndoe"))
            .andExpect(jsonPath("$.email").value("john@example.com"));
    }

}
