package com.ecommerce.lab.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.UserRepository;
import com.ecommerce.lab.service.AuthService;
import com.ecommerce.lab.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserService userService;

    // Toggle 2FA status
    @PostMapping("/toggle")
    public ResponseEntity<?> toggle2FA(Principal principal, @RequestParam boolean enable) {
        User user = userRepository.findByEmail(principal.getName()).get();

        if (enable) {
            authService.generateAndSend2FACode(user.getEmail());
            return ResponseEntity.ok("Verification code sent to email.");
        } else {
            user.set2faEnabled(false);
            user.setTwoFactorCode(null);
            userRepository.save(user);
            return ResponseEntity.ok("2FA has been disabled.");
        }
    }

    // Verify the code
    @PostMapping("/verify")
    public ResponseEntity<?> verify2fa(@RequestBody Map<String, String> body, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String email = (session != null) ? (String) session.getAttribute("PENDING_2FA_USER") : null;

        if (email == null) {
            return ResponseEntity.status(401).body("No login attempt in progress.");
        }

        String code = body.get("code");
        if (authService.verify2FACode(email, code)) {
            User user = userService.findByEmail(email);
            return authService.finalizeSession(user, request);
        }

        return ResponseEntity.status(401).body("Invalid or expired 2FA code.");
    }
}
