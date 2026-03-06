package com.example.kiccdemo.dto;

import com.example.kiccdemo.entity.PaymentRefund;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 환불 이력 조회 응답 DTO입니다. 환불 거래 단위 데이터를 응답 형식으로 변환합니다.
 */
public class RefundHistoryResponse {

    private BigDecimal amount;
    private String refundTransactionId;
    private String reason;
    private LocalDateTime createdAt;

    public static RefundHistoryResponse from(PaymentRefund refund) {
        RefundHistoryResponse response = new RefundHistoryResponse();
        response.amount = refund.getAmount();
        response.refundTransactionId = refund.getRefundTransactionId();
        response.reason = refund.getReason();
        response.createdAt = refund.getCreatedAt();
        return response;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getRefundTransactionId() {
        return refundTransactionId;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
