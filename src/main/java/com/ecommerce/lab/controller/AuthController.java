package com.ecommerce.lab.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

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

    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginReq, HttpServletRequest request) {

        User user = userService.findByEmail(loginReq.email());

        if (!passwordEncoder.matches(loginReq.password(), user.getPassword())) {
            throw new AuthenticationException("Wrong password");
        }

        var auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, Collections.emptyList());

        SecurityContextHolder.getContext().setAuthentication(auth);

        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        return ResponseEntity.ok(Map.of("message", "Logged in successfully", "email", user.getEmail()));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.ok(userService.registerUser(dto));
    }
}