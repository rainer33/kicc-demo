package com.example.kiccdemo.service;

import com.example.kiccdemo.config.AppProperties;
import com.example.kiccdemo.entity.Payment;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
/**
 * KICC 전송용 폼 필드와 서명값을 생성하는 팩토리입니다.
 */
public class KiccPayloadFactory {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AppProperties appProperties;

    public KiccPayloadFactory(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public Map<String, String> createFormFields(Payment payment) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String amount = payment.getAmount().setScale(0).toPlainString();
        String signatureBase = appProperties.getPayment().getKicc().getMerchantId()
                + payment.getOrderId()
                + amount
                + timestamp;

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("mid", appProperties.getPayment().getKicc().getMerchantId());
        fields.put("oid", payment.getOrderId());
        fields.put("goodsName", payment.getOrderName());
        fields.put("buyerName", payment.getBuyerName());
        fields.put("amt", amount);
        fields.put("timestamp", timestamp);
        fields.put("signature", hmacSha256Hex(signatureBase, appProperties.getPayment().getKicc().getMerchantKey()));
        fields.put("returnUrl", appProperties.getFrontendUrl() + "/result?orderId=" + payment.getOrderId());
        fields.put("callbackUrl", "http://localhost:8080/api/payments/kicc/callback");
        return fields;
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
            throw new IllegalStateException("Failed to create signature", e);
        }
    }
}
