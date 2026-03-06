package com.example.kiccdemo.dto;

import com.example.kiccdemo.entity.PaymentRefund;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
