package com.example.kiccdemo.entity;

/**
 * 키오스크 결제 세션 상태입니다.
 */
public enum KioskSessionStatus {
    CREATED,
    PAYMENT_REQUESTED,
    APPROVED,
    FAILED,
    CANCELED
}
