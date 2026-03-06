package com.example.kiccdemo.dto;

import com.example.kiccdemo.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 상태 조회/처리 결과 DTO입니다. 승인/취소/환불 상태를 종합해 반환합니다.
 */
public class PaymentResultResponse {

    private String orderId;
    private BigDecimal amount;
    private String orderName;
    private String buyerName;
    private String status;
    private String pgTransactionId;
    private String failReason;
    private BigDecimal refundedAmount;
    private BigDecimal refundableAmount;
    private LocalDateTime approvedAt;
    private LocalDateTime canceledAt;
    private LocalDateTime updatedAt;

    public static PaymentResultResponse from(Payment payment) {
        PaymentResultResponse response = new PaymentResultResponse();
        response.orderId = payment.getOrderId();
        response.amount = payment.getAmount();
        response.orderName = payment.getOrderName();
        response.buyerName = payment.getBuyerName();
        response.status = payment.getStatus().name();
        response.pgTransactionId = payment.getPgTransactionId();
        response.failReason = payment.getFailReason();
        response.refundedAmount = payment.getRefundedAmount();
        response.refundableAmount = payment.getAmount().subtract(payment.getRefundedAmount());
        response.approvedAt = payment.getApprovedAt();
        response.canceledAt = payment.getCanceledAt();
        response.updatedAt = payment.getUpdatedAt();
        return response;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getOrderName() {
        return orderName;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public String getStatus() {
        return status;
    }

    public String getPgTransactionId() {
        return pgTransactionId;
    }

    public String getFailReason() {
        return failReason;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public BigDecimal getRefundableAmount() {
        return refundableAmount;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
