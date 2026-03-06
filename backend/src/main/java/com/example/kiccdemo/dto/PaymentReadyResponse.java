package com.example.kiccdemo.dto;

import java.util.Map;

/**
 * 결제 준비 완료 응답 DTO입니다. 주문번호, PG URL, 전송 폼 필드를 제공합니다.
 */
public class PaymentReadyResponse {

    private String orderId;
    private String payUrl;
    private boolean mockMode;
    private Map<String, String> formFields;

    public PaymentReadyResponse(String orderId, String payUrl, boolean mockMode, Map<String, String> formFields) {
        this.orderId = orderId;
        this.payUrl = payUrl;
        this.mockMode = mockMode;
        this.formFields = formFields;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public boolean isMockMode() {
        return mockMode;
    }

    public Map<String, String> getFormFields() {
        return formFields;
    }
}
