package com.example.kiccdemo.dto;

import com.example.kiccdemo.entity.KioskPaymentMethod;
import com.example.kiccdemo.entity.KioskSession;
import com.example.kiccdemo.entity.KioskSessionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 키오스크 세션 응답 DTO입니다.
 */
public class KioskSessionResponse {

    private String sessionId;
    private String kioskId;
    private String orderName;
    private BigDecimal amount;
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
        response.status = session.getStatus();
        response.paymentMethod = session.getPaymentMethod();
        response.orderId = session.getOrderId();
        response.terminalTransactionId = session.getTerminalTransactionId();
        response.lastMessage = session.getLastMessage();
        response.createdAt = session.getCreatedAt();
        response.updatedAt = session.getUpdatedAt();
        return response;
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
