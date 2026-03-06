package com.example.kiccdemo.service;

import com.example.kiccdemo.config.AppProperties;
import org.springframework.stereotype.Service;

/**
 * 관리자 토큰 검증 서비스입니다. 토큰 정책(필수/길이)을 강제하고 요청 인증을 수행합니다.
 *
 * 보안 의도:
 * - 서버 기동 시점에 토큰 정책을 강제해 \"설정 누락 상태\"로 운영되는 것을 방지합니다.
 * - API 호출 시점에는 헤더 토큰과 설정 토큰의 완전 일치를 요구합니다.
 */
@Service
public class AdminAuthService {

    private final AppProperties appProperties;

    /** 애플리케이션 시작 시 관리자 토큰 정책을 검증합니다. */
    public AdminAuthService(AppProperties appProperties) {
        this.appProperties = appProperties;
        String token = appProperties.getSecurity().getAdminToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("ADMIN_TOKEN must be set");
        }
        if (token.length() < 16) {
            throw new IllegalStateException("ADMIN_TOKEN must be at least 16 characters");
        }
    }

    /** 요청 헤더의 관리자 토큰을 검증합니다. */
    public void validateToken(String token) {
        if (token == null || token.isBlank() || !token.equals(appProperties.getSecurity().getAdminToken())) {
            throw new SecurityException("Invalid admin token");
        }
    }
}
