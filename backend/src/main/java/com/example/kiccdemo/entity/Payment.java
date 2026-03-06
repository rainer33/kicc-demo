package com.example.kiccdemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
/**
 * 결제 트랜잭션 엔티티입니다. 승인/취소/실패/환불 누적 금액 등 핵심 상태를 보관합니다.
 */
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String orderId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 120)
    private String orderName;

    @Column(nullable = false, length = 60)
    private String buyerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 120)
    private String pgTransactionId;

    @Column(length = 255)
    private String failReason;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime approvedAt;
    private LocalDateTime canceledAt;

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getPgTransactionId() {
        return pgTransactionId;
    }

    public void setPgTransactionId(String pgTransactionId) {
        this.pgTransactionId = pgTransactionId;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount == null ? BigDecimal.ZERO : refundedAmount;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public void markReady() {
        this.status = PaymentStatus.READY;
        this.failReason = null;
        this.refundedAmount = BigDecimal.ZERO;
        this.canceledAt = null;
    }

    public void markApproved(String pgTransactionId) {
        this.status = PaymentStatus.APPROVED;
        this.pgTransactionId = pgTransactionId;
        this.failReason = null;
        this.approvedAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failReason = reason;
    }

    public void markCanceled() {
        this.status = PaymentStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
        this.refundedAmount = amount;
    }

    public void addRefund(BigDecimal refundAmount) {
        this.refundedAmount = getRefundedAmount().add(refundAmount);
        if (this.refundedAmount.compareTo(this.amount) >= 0) {
            this.refundedAmount = this.amount;
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
    }

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.refundedAmount == null) {
            this.refundedAmount = BigDecimal.ZERO;
        }
    }

    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
