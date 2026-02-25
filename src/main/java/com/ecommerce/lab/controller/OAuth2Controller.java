package com.ecommerce.lab.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.lab.model.Role;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/oauth2-success")
    public ResponseEntity<?> handleOAuth2Success(@AuthenticationPrincipal OAuth2User principal,
            HttpServletRequest request) {
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");
        String picture = principal.getAttribute("picture");

        // Check if user exists
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isEmpty()) {
            // Register new user
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setUserName(name);
            user.setProfilePicture(picture);
            String randomPassword = UUID.randomUUID().toString();
            user.setPassword(passwordEncoder.encode(randomPassword));
            user.setRole(Role.ROLE_USER);
            userRepository.save(user);
        } else {
            user = userOptional.get();
        }

        // Establish Session (Manually since we want a unified session)
        var auth = new UsernamePasswordAuthenticationToken(email, null,
                List.of(new SimpleGrantedAuthority(user.getRole().name())));
        SecurityContextHolder.getContext().setAuthentication(auth);

        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        // Redirect to frontend home/profile
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/profile").build();
    }
}