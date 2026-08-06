package com.example.kiccdemo.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.kiccdemo.entity.KioskPaymentMethod;
import com.example.kiccdemo.entity.KioskSession;
import com.example.kiccdemo.entity.KioskSessionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 키오스크 세션 응답 DTO입니다.
 */
public class KioskSessionResponse {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String sessionId;
    private String kioskId;
    private String orderName;
    private BigDecimal amount;
    private String sourceSystem;
    private String externalProductId;
    private String customerName;
    private String customerPhone;
    private String itemSummary;
    private JsonNode productMetadata;
    private KioskSessionStatus status;
    private KioskPaymentMethod paymentMethod;
    private String orderId;
    private String terminalTransactionId;
    private String lastMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KioskSessionResponse from(KioskSession session) {
        KioskSessionResponse response = new KioskSessionResponse();
        response.sessionId = session.getSessionId();
        response.kioskId = session.getKioskId();
        response.orderName = session.getOrderName();
        response.amount = session.getAmount();
        response.sourceSystem = session.getSourceSystem();
        response.externalProductId = session.getExternalProductId();
        response.customerName = session.getCustomerName();
        response.customerPhone = session.getCustomerPhone();
        response.itemSummary = session.getItemSummary();
        response.productMetadata = parseJson(session.getProductMetadataJson());
        response.status = session.getStatus();
        response.paymentMethod = session.getPaymentMethod();
        response.orderId = session.getOrderId();
        response.terminalTransactionId = session.getTerminalTransactionId();
        response.lastMessage = session.getLastMessage();
        response.createdAt = session.getCreatedAt();
        response.updatedAt = session.getUpdatedAt();
        return response;
    }

    private static JsonNode parseJson(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getKioskId() {
        return kioskId;
    }

    public String getOrderName() {
        return orderName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public String getExternalProductId() {
        return externalProductId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getItemSummary() {
        return itemSummary;
    }

    public JsonNode getProductMetadata() {
        return productMetadata;
    }

    public KioskSessionStatus getStatus() {
        return status;
    }

    public KioskPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getTerminalTransactionId() {
        return terminalTransactionId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
