package com.example.kiccdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
/**
 * 프론트엔드 도메인에서 백엔드 API를 호출할 수 있도록 CORS 정책을 설정합니다.
 */
public class CorsConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    public CorsConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(appProperties.getFrontendUrl())
                .allowedMethods("GET", "POST")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}
