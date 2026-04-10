package com.aimpl.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig {

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void configureXssProtection() {
        SimpleModule xssModule = new SimpleModule("XssCleanModule");
        xssModule.addDeserializer(String.class, new XssCleanDeserializer());
        objectMapper.registerModule(xssModule);
    }
}
