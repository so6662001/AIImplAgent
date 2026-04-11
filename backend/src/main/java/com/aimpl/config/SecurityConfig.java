package com.aimpl.config;

import com.aimpl.domain.auth.service.JwtService;
import com.aimpl.domain.qa.service.ClientAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService, ObjectMapper objectMapper, Environment env) {
        JwtAuthFilter filter = new JwtAuthFilter(jwtService, objectMapper);
        boolean isTest = Arrays.asList(env.getActiveProfiles()).contains("test");
        if (isTest) {
            filter.setEnabled(false);
        }
        return filter;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter jwtAuthFilter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthFilter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public ClientAuthFilter clientAuthFilter(ClientAuthService clientAuthService, ObjectMapper objectMapper, Environment env) {
        ClientAuthFilter filter = new ClientAuthFilter(clientAuthService, objectMapper);
        boolean isTest = Arrays.asList(env.getActiveProfiles()).contains("test");
        if (isTest) {
            filter.setEnabled(false);
        }
        return filter;
    }

    @Bean
    public FilterRegistrationBean<ClientAuthFilter> clientFilterRegistration(ClientAuthFilter clientAuthFilter) {
        FilterRegistrationBean<ClientAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(clientAuthFilter);
        registration.addUrlPatterns("/api/client/*");
        registration.setOrder(2);
        return registration;
    }
}
