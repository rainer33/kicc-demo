package com.example.kiccdemo.service;

import com.example.kiccdemo.config.AppProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * KICC 콜백 서명 검증 컴포넌트입니다. 위변조 요청을 차단합니다.
 *
 * 검증 규칙:
 * - 콜백 서명 필수 옵션이 꺼져 있으면 검증을 생략합니다(개발 편의 모드).
 * - 켜져 있으면 X-KICC-SIGNATURE 헤더를 반드시 요구합니다.
 * - payload(orderId|resultCode|txId) 기반 HMAC-SHA256 hex를 비교합니다.
 */
@Component
public class KiccWebhookVerifier {

    private final AppProperties appProperties;

    public KiccWebhookVerifier(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /** 외부에서 전달된 콜백 서명이 유효한지 검증합니다. */
    public void verify(String orderId, String resultCode, String txId, String signatureHeader) {
        if (!appProperties.getPayment().getKicc().isCallbackSignatureRequired()) {
            return;
        }
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new SecurityException("Missing callback signature");
        }
        String payload = orderId + "|" + resultCode + "|" + (txId == null ? "" : txId);
        String expected = hmacSha256Hex(payload, appProperties.getPayment().getKicc().getMerchantKey());
        if (!expected.equalsIgnoreCase(signatureHeader)) {
            throw new SecurityException("Invalid callback signature");
        }
    }

    /** HMAC-SHA256 hex 문자열 생성 유틸리티 */
    private String hmacSha256Hex(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to verify signature", e);
        }
    }
}
