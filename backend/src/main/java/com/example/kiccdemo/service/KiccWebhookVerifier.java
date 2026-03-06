package com.example.kiccdemo.service;

import com.example.kiccdemo.config.AppProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class KiccWebhookVerifier {

    private final AppProperties appProperties;

    public KiccWebhookVerifier(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

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
