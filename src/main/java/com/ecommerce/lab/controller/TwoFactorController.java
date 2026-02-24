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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/account/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final AuthService authService;
    private final UserRepository userRepository;

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
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request, Principal principal) {
        String code = request.get("code");
        boolean isValid = authService.verify2FACode(principal.getName(), code);

        if (isValid) {
            return ResponseEntity.ok("Verification successful.");
        }
        return ResponseEntity.status(401).body("Invalid or expired code.");
    }
}
