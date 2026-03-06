package com.example.kiccdemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "kiosk_sessions")
/**
 * 키오스크 결제 시나리오를 재현하기 위한 세션 엔티티입니다.
 *
 * 설계 목적:
 * - 한 번의 키오스크 주문/결제 흐름을 sessionId 기준으로 추적
 * - 단말 승인/실패 이벤트를 재생해도 최종 상태를 재현 가능
 */
public class KioskSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String sessionId;

    @Column(nullable = false, length = 40)
    private String kioskId;

    @Column(nullable = false, length = 120)
    private String orderName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KioskSessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private KioskPaymentMethod paymentMethod;

    @Column(length = 64)
    private String orderId;

    @Column(length = 80)
    private String terminalTransactionId;

    @Column(length = 600)
    private String lastMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getKioskId() {
        return kioskId;
    }

    public void setKioskId(String kioskId) {
        this.kioskId = kioskId;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public KioskSessionStatus getStatus() {
        return status;
    }

    public void setStatus(KioskSessionStatus status) {
        this.status = status;
    }

    public KioskPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(KioskPaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTerminalTransactionId() {
        return terminalTransactionId;
    }

    public void setTerminalTransactionId(String terminalTransactionId) {
        this.terminalTransactionId = terminalTransactionId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
