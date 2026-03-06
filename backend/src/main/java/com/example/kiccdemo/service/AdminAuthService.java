package com.example.kiccdemo.service;

import com.example.kiccdemo.config.AppProperties;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final AppProperties appProperties;

    public AdminAuthService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public void validateToken(String token) {
        if (token == null || token.isBlank() || !token.equals(appProperties.getSecurity().getAdminToken())) {
            throw new SecurityException("Invalid admin token");
        }
    }
}
