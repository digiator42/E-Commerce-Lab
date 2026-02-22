package com.ecommerce.lab.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Static Resources & Auth
                        .requestMatchers("/", "/index.html", "/static/**", "/js/**", "/css/**", "/components/**")
                        .permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers("/login", "/register", "/product/**", "/cart").permitAll()

                        // Public Browsing (Allow GET only)
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/reviews/**")
                        .permitAll()

                        // Admin Only
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Authenticated Actions
                        .requestMatchers("/api/orders/**", "/api/cart/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").authenticated()

                        // Catch-all
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        }));

        return http.build();
    }
}