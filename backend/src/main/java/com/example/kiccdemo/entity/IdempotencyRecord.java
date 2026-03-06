package com.example.kiccdemo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records")
/**
 * 멱등키 처리 상태를 저장하는 엔티티입니다. 중복 결제 요청을 안전하게 제어합니다.
 */
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String idemKey;

    @Column(length = 64)
    private String orderId;

    @Column(nullable = false, length = 64)
    private String requestHash;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public String getIdemKey() {
        return idemKey;
    }

    public void setIdemKey(String idemKey) {
        this.idemKey = idemKey;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
