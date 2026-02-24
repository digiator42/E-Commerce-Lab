package com.ecommerce.lab.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.UserRepository;

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
            userRepository.save(user);
            return true;
        }
        return false;
    }
}