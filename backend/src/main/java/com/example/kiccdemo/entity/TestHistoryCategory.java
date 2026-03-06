package com.example.kiccdemo.entity;

/**
 * 테스트 이력의 유형을 분류하는 열거형입니다.
 *
 * 분류 목적:
 * - 어떤 종류의 리스크(동시성, 네트워크, 보안 등)를 검증했는지 빠르게 식별
 * - 운영 중 장애 발생 시 과거에 어떤 유형의 회귀 테스트를 통과했는지 추적
 */
public enum TestHistoryCategory {
    PAYMENT_FLOW,
    IDEMPOTENCY,
    CONCURRENCY,
    NETWORK_FAILURE,
    CALLBACK_SECURITY,
    REFUND,
    CANCEL,
    RECONCILIATION,
    INFRA
}
