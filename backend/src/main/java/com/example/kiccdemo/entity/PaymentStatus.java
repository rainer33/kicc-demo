package com.example.kiccdemo.entity;

/**
 * 결제 상태를 표현하는 열거형입니다. 준비/승인/취소/부분환불/전액환불/실패를 관리합니다.
 */
public enum PaymentStatus {
    READY,
    APPROVED,
    CANCELED,
    PARTIALLY_REFUNDED,
    REFUNDED,
    FAILED
}
