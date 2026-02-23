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

import com.ecommerce.lab.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.ecommerce.lab.dto.LoginRequestDTO;
import com.ecommerce.lab.dto.RegisterRequestDTO;
import com.ecommerce.lab.dto.UserResponseDTO;
import com.ecommerce.lab.exception.AuthenticationException;
import com.ecommerce.lab.model.Role;
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

    @GetMapping("is-logged-in")
    public ResponseEntity<?> isLoggedIn(Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("isLoggedIn", false));
        }

        User user = userService.findByEmail(principal.getName());

        // String role = user.getRole() != null ? user.getRole().name() : Role.ROLE_USER.name();

        return ResponseEntity.ok(UserResponseDTO.fromEntity(user));

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginReq, HttpServletRequest request) {

        User user = userService.findByEmail(loginReq.email());

        if (!passwordEncoder.matches(loginReq.password(), user.getPassword())) {
            throw new AuthenticationException("Wrong password");
        }

        String role = user.getRole() != null ? user.getRole().name() : Role.ROLE_USER.name();

        var authorities = List.of(new SimpleGrantedAuthority(role));

        var auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, authorities);

        SecurityContextHolder.getContext().setAuthentication(auth);

        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        return ResponseEntity.ok(UserResponseDTO.fromEntity(user));
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