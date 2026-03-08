package com.ecommerce.lab.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class SpaConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/", "classpath:/templates/")
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
                @Override
                protected Resource getResource(String resourcePath, Resource location)
                    throws IOException {
                    Resource requestedResource = location.createRelative(resourcePath);

                    if (requestedResource.exists() && requestedResource.isReadable()) {
                        return requestedResource;
                    }

                    if (resourcePath.startsWith("api/") || resourcePath.startsWith("/api/")) {
                        return null;
                    }

                    Resource index = location.createRelative("index.html");
                    if (index.exists() && index.isReadable()) {
                        return index;
                    }

                    return null;
                }
            });
    }
}