package com.ecommerce.lab.filter;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.base.UserRepository;
import com.ecommerce.lab.service.CustomUserDetailsService;
import com.ecommerce.lab.service.TokenBlacklistService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final String[] PUBLIC_PATHS = {
            "/",
            "/index.html",
            "/static/**",
            "/js/**",
            "/css/**",
            "/components",
            "/favicon.ico",
            "/api/auth/**",
            "/api/products/**",
            "/api/categories/**",
            "/api/reviews/**",
            "/api/2fa/verify",
            "/api/2fa/resend"
    };

    private JwtUtils jwtUtils;

    private CustomUserDetailsService userDetailsService;

    private TokenBlacklistService blacklistService;

    private UserRepository userRepository;

    public JwtAuthenticationFilter(
        JwtUtils jwtUtils,
        CustomUserDetailsService userDetailsService,
        TokenBlacklistService blacklistService,
        UserRepository userRepository
    ) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.blacklistService = blacklistService;
        this.userRepository = userRepository;
    }

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // SKIP JWT validation for permitAll paths
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return Arrays.stream(PUBLIC_PATHS)
            .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    )
        throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String path = request.getRequestURI();

        // Sensitive API and header is missing
        if (path.startsWith("/api/admin")) {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Unauthorized");
                return;
            }
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (blacklistService.isBlacklisted(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has been logged out");
                return;
            }

            String email = jwtUtils.extractUsername(token, request);

            // Toke is expired, signature errors
            if (email == null) {
                // The token was present but invalid (signature mismatch or expired)
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or Expired Token");
                return; // STOP the filter chain here
            }

            Authentication existingAuth = SecurityContextHolder.getContext()
                .getAuthentication();

            // Only skip if the current user is already the SAME user
            if (existingAuth == null || !existingAuth.getName().equals(email)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                User user = userRepository.findByEmail(email).orElse(null);

                System.out.println("===> Is the same token ? " + token.equals(user.getToken()));

                if (jwtUtils.validateToken(token, userDetails, request)
                    && token.equals(user.getToken())) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );
                    authToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Overwrite or set the authentication
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // If the token in the DB is null or different, the user has logged out
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}