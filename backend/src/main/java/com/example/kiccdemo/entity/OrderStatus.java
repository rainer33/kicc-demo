package com.example.kiccdemo.entity;

/**
 * 주문 상태를 표현하는 열거형입니다. 결제 진행 단계와 환불 단계를 포함합니다.
 */
public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAID,
    PARTIALLY_REFUNDED,
    REFUNDED,
    CANCELED,
    FAILED
}
