package com.example.kiccdemo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 결제 준비 요청 DTO입니다. 주문명/구매자/금액 입력값 검증 규칙을 포함합니다.
 */
public class PaymentReadyRequest {

    @NotBlank
    private String orderName;

    @NotBlank
    private String buyerName;

    @NotNull
    @DecimalMin(value = "100.00")
    @Digits(integer = 13, fraction = 0)
    private BigDecimal amount;

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
