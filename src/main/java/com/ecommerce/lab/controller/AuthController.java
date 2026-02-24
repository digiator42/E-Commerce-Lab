package com.ecommerce.lab.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.ecommerce.lab.service.AuthService;
import com.ecommerce.lab.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.ecommerce.lab.dto.LoginRequestDTO;
import com.ecommerce.lab.dto.RegisterRequestDTO;
import com.ecommerce.lab.dto.UserResponseDTO;
import com.ecommerce.lab.exception.AuthenticationException;
import com.ecommerce.lab.model.User;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, AuthService authService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @GetMapping("is-logged-in")
    public ResponseEntity<?> isLoggedIn(Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("isLoggedIn", false));
        }

        User user = userService.findByEmail(principal.getName());

        // String role = user.getRole() != null ? user.getRole().name() :
        // Role.ROLE_USER.name();

        return ResponseEntity.ok(UserResponseDTO.fromEntity(user));

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginReq, HttpServletRequest request) {

        User user = userService.findByEmail(loginReq.email());

        if (!passwordEncoder.matches(loginReq.password(), user.getPassword())) {
            throw new AuthenticationException("Wrong password");
        }

        if (user.is2faEnabled()) {
            authService.generateAndSend2FACode(user.getEmail());

            HttpSession session = request.getSession(true);
            session.setAttribute("PENDING_2FA_USER", user.getEmail());

            return ResponseEntity.ok(Map.of(
                    "requires2FA", true,
                    "message", "Please enter the code sent to your email"));
        }

        return authService.finalizeSession(user, request);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.ok(userService.registerUser(dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Principal principal, HttpServletRequest request) {

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(true);
        session.invalidate();

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));

    }
}