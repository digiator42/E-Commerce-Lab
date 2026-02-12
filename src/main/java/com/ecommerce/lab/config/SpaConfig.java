package com.ecommerce.lab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpaConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect all non-API paths to index.html to support SPA routing
        registry.addViewController("/{path:[^\\.]*}")
                .setViewName("forward:/index.html");
    }
}