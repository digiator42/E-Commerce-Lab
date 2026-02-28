package com.ecommerce.lab.service;

import org.springframework.stereotype.Service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@Service
public class TotpService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    // Generate a new secret for the user
    public String generateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    // Generate the QR Code URL
    public String getQrCodeUrl(String email, String secret) {
        // Format: otpauth://totp/Issuer:Account?secret=SECRET&issuer=Issuer
        return "otpauth://totp/CommerceLab:" + email + "?secret=" + secret + "&issuer=CommerceLab";
    }

    // Verify the 6-digit code provided by the user
    public boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}