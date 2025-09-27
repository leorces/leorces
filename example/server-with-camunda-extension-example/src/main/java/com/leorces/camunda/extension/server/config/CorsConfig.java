package com.leorces.camunda.extension.server.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * CORS configuration to allow cross-origin requests from all origins.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final String API_PATH_PATTERN = "/api/**";


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(API_PATH_PATTERN)
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

}