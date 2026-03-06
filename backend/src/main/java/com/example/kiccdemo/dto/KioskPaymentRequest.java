package com.example.kiccdemo.dto;

import com.example.kiccdemo.entity.KioskPaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * 키오스크 결제요청 DTO입니다.
 */
public class KioskPaymentRequest {

    @NotNull
    private KioskPaymentMethod paymentMethod;

    public KioskPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(KioskPaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
