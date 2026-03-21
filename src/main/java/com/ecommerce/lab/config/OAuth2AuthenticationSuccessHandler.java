package com.ecommerce.lab.config;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.ecommerce.lab.filter.JwtUtils;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.base.UserRepository;
import com.ecommerce.lab.service.CustomUserDetailsService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RememberMeServices rememberMeServices;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // Generate Token
        String token = jwtUtils.generateToken(email, false);

        // Update User in DB with token/last login
        User user = userRepository.findByEmail(email)
            .map(u -> {
                u.setLastLogin(LocalDateTime.now());
                u.setToken(token);
                return userRepository.save(u);
            })
            .orElseThrow(() -> new ServletException("User not found after OAuth2 login"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            userDetails,
            userDetails.getPassword(),
            userDetails.getAuthorities()
        );

        rememberMeServices.loginSuccess(request, response, auth);

        SecurityContextHolder.getContext().setAuthentication(auth);

        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        // Trigger RememberMe
        rememberMeServices.loginSuccess(request, response, auth);

        // Redirect to your success API or directly to Frontend
        getRedirectStrategy()
            .sendRedirect(request, response, "/api/auth/oauth2-success?token=" + token);
    }
}