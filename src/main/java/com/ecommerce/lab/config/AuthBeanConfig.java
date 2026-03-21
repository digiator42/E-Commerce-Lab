package com.ecommerce.lab.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class AuthBeanConfig {

    @Value("${spring.security.remember-key}")
    private String REMEMBER_ME_KEY;

    @Bean
    public RememberMeServices rememberMeServices(UserDetailsService userDetailsService) {
        // We override the 'rememberMeRequested' method to ignore the request parameters
        // since we are handling the logic manually in our controllers/handlers.
        TokenBasedRememberMeServices services = new TokenBasedRememberMeServices(
            REMEMBER_ME_KEY,
            userDetailsService
        ) {
            @Override
            protected boolean rememberMeRequested(HttpServletRequest request, String parameter) {
                // Check if we manually flagged this request as 'remember-me' in our service
                Object manualFlag = request.getAttribute("FORCE_REMEMBER_ME");
                if (manualFlag != null && (boolean) manualFlag) {
                    return true;
                }
                return super.rememberMeRequested(request, parameter);
            }
        };

        services.setAlwaysRemember(false); // Keeps OAuth2 from crashing
        return services;
    }
}