package com.ecommerce.lab.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OAuth2Controller {


    @GetMapping("/oauth2-success")
    public ResponseEntity<?> handleOAuth2Success(
        @AuthenticationPrincipal OAuth2User principal,
        HttpServletRequest request,
        HttpServletResponse response
    ) {

        return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/profile").build();
    }
}