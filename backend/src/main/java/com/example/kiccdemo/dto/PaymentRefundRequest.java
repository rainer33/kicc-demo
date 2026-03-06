package com.example.kiccdemo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 환불 요청 DTO입니다. 환불 금액과 사유를 전달하며 금액 유효성을 검증합니다.
 */
public class PaymentRefundRequest {

    @NotNull
    @DecimalMin(value = "1.00")
    @Digits(integer = 13, fraction = 0)
    private BigDecimal amount;

    private String reason;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
