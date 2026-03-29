package com.ecommerce.lab.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.stereotype.Service;

import com.ecommerce.lab.dto.LoginRequestDTO;
import com.ecommerce.lab.dto.UserResponseDTO;
import com.ecommerce.lab.exception.ResourceNotFoundException;
import com.ecommerce.lab.filter.JwtUtils;
import com.ecommerce.lab.model.Role;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.base.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    @Autowired
    private RememberMeServices rememberMeServices;
    private final CustomUserDetailsService userDetailsService;

    public void generateAndSend2FACode(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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

    public UserResponseDTO finalizeSession(
        User user,
        LoginRequestDTO loginReq,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        String role = user.getRole() != null ? user.getRole().toString()
            : Role.ROLE_USER.toString();

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        var authorities = List.of(new SimpleGrantedAuthority(role));
        var auth = new UsernamePasswordAuthenticationToken(
            userDetails, userDetails.getPassword(), authorities
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        final boolean isRememberMe = StringUtils
            .equals(loginReq != null ? loginReq.rememberMe() : "", "on");

        // Generate the token
        String token = jwtUtils.generateToken(
            user.getEmail(),
            isRememberMe
        );

        if (isRememberMe) {
            request.setAttribute("FORCE_REMEMBER_ME", true);

            UsernamePasswordAuthenticationToken rememberMeAuth = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
            );

            rememberMeServices.loginSuccess(request, response, rememberMeAuth);
        }

        // Clear any pending 2FA data
        session.removeAttribute("PENDING_2FA_USER");

        user.setToken(token);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return UserResponseDTO.fromEntity(user);
    }
}