package com.ecommerce.lab.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.UserRepository;
import com.ecommerce.lab.service.TotpService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/2fa/totp")
@RequiredArgsConstructor
public class TotpController {

    private final TotpService totpService;
    private final UserRepository userRepository;

    @PostMapping("/setup")
    public ResponseEntity<?> setupTotp(Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).get();
        
        // Generate and save secret
        String secret = totpService.generateSecret();
        user.setTotpSecret(secret);
        userRepository.save(user);

        // Return QR Code URL to Frontend
        String qrCodeUrl = totpService.getQrCodeUrl(user.getEmail(), secret);
        return ResponseEntity.ok(Map.of("qrCodeUrl", qrCodeUrl));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmTotp(@RequestBody Map<String, Integer> request, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).get();
        boolean isValid = totpService.verifyCode(user.getTotpSecret(), request.get("code"));

        if (isValid) {
            user.setTotpEnabled(true);
            userRepository.save(user);
            return ResponseEntity.ok("Google Authenticator enabled successfully!");
        }
        return ResponseEntity.status(400).body("Invalid code. Please try again.");
    }
}