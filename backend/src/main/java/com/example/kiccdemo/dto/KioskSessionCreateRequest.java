package com.example.kiccdemo.dto;

import com.fasterxml.jackson.databind.JsonNode;
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

    private String sourceSystem;

    private String externalProductId;

    private String customerName;

    private String customerPhone;

    private String itemSummary;

    private JsonNode productMetadata;

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

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getExternalProductId() {
        return externalProductId;
    }

    public void setExternalProductId(String externalProductId) {
        this.externalProductId = externalProductId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getItemSummary() {
        return itemSummary;
    }

    public void setItemSummary(String itemSummary) {
        this.itemSummary = itemSummary;
    }

    public JsonNode getProductMetadata() {
        return productMetadata;
    }

    public void setProductMetadata(JsonNode productMetadata) {
        this.productMetadata = productMetadata;
    }
}
