package com.ecommerce.lab.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import com.ecommerce.lab.filter.JwtAuthenticationFilter;
import com.ecommerce.lab.repository.UserRepository;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    private final UserRepository userRepository;
    private final String REMEMBER_ME_KEY = "a2V5MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=";

    public SecurityConfig(UserRepository userRepository) { this.userRepository = userRepository; }

    @Bean
    public RememberMeServices rememberMeServices() {
        TokenBasedRememberMeServices services = new TokenBasedRememberMeServices(
            REMEMBER_ME_KEY, userDetailsService()
        );
        // This tells the service to skip the request.getParameter("remember-me") check
        // and just trust that if loginSuccess is called.
        services.setAlwaysRemember(true);
        return services;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
            .map(
                user -> (UserDetails) org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .authorities(user.getRole().toString())
                    .build()
            )
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

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

                    // Frontend Routes & Error Path
                    .requestMatchers("/login", "/register", "/product/**", "/cart").permitAll()

                    // API Rules
                    .requestMatchers("/api/auth/**", "/api/2fa/**").permitAll()

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
            .rememberMe(
                remember -> remember
                    .key(REMEMBER_ME_KEY)
                    .rememberMeParameter("remember-me")
                    .userDetailsService(userDetailsService())
                    .rememberMeServices(rememberMeServices())
            )
            .oauth2Login(
                oauth2 -> oauth2
                    .loginPage("/login")
                    .defaultSuccessUrl("/api/auth/oauth2-success", true)
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