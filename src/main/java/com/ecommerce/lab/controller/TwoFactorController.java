package com.ecommerce.lab.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.UserRepository;
import com.ecommerce.lab.service.AuthService;
import com.ecommerce.lab.service.TotpService;
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
    private final TotpService totpService;

    // Toggle 2FA status
    @PostMapping("/toggle")
    public ResponseEntity<?> toggle2FA(Principal principal, @RequestBody Map<String, String> enabled) {
        User user = userRepository.findByEmail(principal.getName()).get();

        if (enabled.get("enabled").equals("true")) {
            // authService.generateAndSend2FACode(user.getEmail());
            user.set2faEnabled(true);
            userRepository.save(user);
            return ResponseEntity.ok("2FA is enabled.");
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

        if (email == null)
            return ResponseEntity.status(401).body("Session expired");

        String inputCode = body.get("code");
        User user = userService.findByEmail(email);

        // Try Method A: TOTP (Google Authenticator)
        try {
            int totpCode = Integer.parseInt(inputCode);
            if (totpService.verifyCode(user.getTotpSecret(), totpCode)) {
                return authService.finalizeSession(user, request);
            }
        } catch (NumberFormatException e) {
            // Not a valid number, so it's definitely not a TOTP code
        }

        // Try Method B: Email Code
        if (authService.verify2FACode(email, inputCode)) {
            return authService.finalizeSession(user, request);
        }

        return ResponseEntity.status(401).body("Invalid code from both App and Email.");
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resend2fa(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String email = (session != null) ? (String) session.getAttribute("PENDING_2FA_USER") : null;

        if (email == null) {
            return ResponseEntity.status(401).body("No active login session found.");
        }

        LocalDateTime lastSent = (LocalDateTime) session.getAttribute("LAST_2FA_SENT");
        if (lastSent != null && lastSent.plusSeconds(60).isAfter(LocalDateTime.now())) {
            return ResponseEntity.status(429).body("Please wait 60 seconds before requesting a new code.");
        }

        // Trigger new code
        authService.generateAndSend2FACode(email);
        session.setAttribute("LAST_2FA_SENT", LocalDateTime.now());

        return ResponseEntity.ok("A new verification code has been sent to your email.");
    }

    @GetMapping("/status")
    public ResponseEntity<?> getUserStatus(Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).build();

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Return a DTO with just the status info
        Map<String, Object> status = new HashMap<>();
        status.put("email", user.getEmail());
        status.put("name", user.getName());
        status.put("is2faEnabled", user.is2faEnabled());
        status.put("role", user.getRole());

        return ResponseEntity.ok(status);
    }
}
