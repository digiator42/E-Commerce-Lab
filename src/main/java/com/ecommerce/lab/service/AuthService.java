package com.ecommerce.lab.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ecommerce.lab.dto.UserResponseDTO;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public void generateAndSend2FACode(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate a random 6-digit code
        String code = String.valueOf(new Random().nextInt(899999) + 100000);

        user.setTwoFactorCode(code);
        user.setTwoFactorCodeExpires(LocalDateTime.now().plusMinutes(5));
        user.set2faEnabled(true);
        userRepository.save(user);

        emailService.send2FACode(email, code);
    }

    public boolean verify2FACode(String email, String code) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTwoFactorCode() != null &&
            user.getTwoFactorCode().equals(code) &&
            user.getTwoFactorCodeExpires().isAfter(LocalDateTime.now())) {

            // Clear code after successful verification
            user.setTwoFactorCode(null);
            user.setTwoFactorCodeExpires(null);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public ResponseEntity<UserResponseDTO> finalizeSession(User user, HttpServletRequest request) {
        String role = user.getRole() != null ? user.getRole().name() : "ROLE_USER";
        var authorities = List.of(new SimpleGrantedAuthority(role));
        var auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);

        SecurityContextHolder.getContext().setAuthentication(auth);
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        // Clear any pending 2FA data
        session.removeAttribute("PENDING_2FA_USER");

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok(UserResponseDTO.fromEntity(user));
    }
}