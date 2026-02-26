package com.ecommerce.lab.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public void createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate a unique UUID token
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpires(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        String resetLink = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/reset-password")
                .queryParam("token", token).toUriString();
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

    public void updatePassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (user.getResetTokenExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null); // Clear the token so it can't be used again
        user.setResetTokenExpires(null);
        userRepository.save(user);
    }
}