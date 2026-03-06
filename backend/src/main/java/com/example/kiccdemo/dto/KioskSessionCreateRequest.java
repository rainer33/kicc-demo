package com.example.kiccdemo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 키오스크 세션 생성 요청 DTO입니다.
 */
public class KioskSessionCreateRequest {

    @NotBlank
    private String kioskId;

    @NotBlank
    private String orderName;

    @NotNull
    @DecimalMin(value = "100.00")
    @Digits(integer = 13, fraction = 0)
    private BigDecimal amount;

    public String getKioskId() {
        return kioskId;
    }

    public void setKioskId(String kioskId) {
        this.kioskId = kioskId;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
