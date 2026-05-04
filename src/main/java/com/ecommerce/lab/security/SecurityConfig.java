package com.ecommerce.lab.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ecommerce.lab.config.OAuth2AuthenticationSuccessHandler;
import com.ecommerce.lab.filter.JwtAuthenticationFilter;
import com.ecommerce.lab.service.CustomOAuth2UserService;
import com.ecommerce.lab.service.CustomUserDetailsService;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;
    private final RememberMeServices rememberMeServices;

    @Value("${spring.security.remember-key}")
    private String REMEMBER_ME_KEY;

    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthFilter,
        CustomUserDetailsService userDetailsService,
        CustomOAuth2UserService customOAuth2UserService,
        OAuth2AuthenticationSuccessHandler oauth2SuccessHandler,
        RememberMeServices rememberMeServices
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
        this.rememberMeServices = rememberMeServices;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(
                auth -> auth
                    // MUST allow FORWARD dispatchers for the ExceptionHandler to work
                    .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()

                    // Static Resources
                    .requestMatchers(
                        "/", "/index.html", "/static/**", "/js/**", "/css/**", "/components/**",
                        "/favicon.ico"
                    )
                    .permitAll()

                    .requestMatchers(
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                    ).permitAll()

                    // Frontend Routes & Error Path
                    .requestMatchers("/login", "/register", "/product/**", "/cart").permitAll()

                    // API Rules
                    .requestMatchers("/api/auth/**", "/api/2fa/**").permitAll()
                    .requestMatchers("/login/oauth2/**", "/oauth2/**").permitAll()

                    .requestMatchers(
                        HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/reviews/**"
                    )
                    .permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    // Catch-all
                    .anyRequest().authenticated()
            )
            .formLogin(
                form -> form
                    .loginPage("/login")
                    .permitAll()
            )
            .oauth2Login(
                oauth2 -> oauth2
                    .loginPage("/login")
                    .userInfoEndpoint(
                        userInfo -> userInfo
                            .userService(customOAuth2UserService)
                    )
                    .successHandler(oauth2SuccessHandler)
                    .failureHandler((request, response, exception) -> {
                        System.out.println("OAuth2 Login Failed: " + exception.getMessage());
                        exception.printStackTrace();
                        response.sendRedirect("/login?error=" + exception.getMessage());
                    })
            )
            .rememberMe(
                remember -> remember
                    .key(REMEMBER_ME_KEY)
                    .rememberMeParameter("remember-me")
                    .userDetailsService(userDetailsService)
                    .rememberMeServices(rememberMeServices)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            .sessionManagement(
                session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .exceptionHandling(
                exception -> exception
                    .authenticationEntryPoint((request, response, authException) -> {
                        // API request
                        if (request.getRequestURI().startsWith("/api")) {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        } else {
                            // If a browser page fails
                            request.getRequestDispatcher("/index.html").forward(request, response);
                        }
                    })
            );

        return http.build();
    }
}