package com.example.kiccdemo.dto;

import com.example.kiccdemo.entity.OrderEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 관리자 주문 조회 응답 DTO입니다. 주문 엔티티를 화면/API 출력 형태로 변환합니다.
 */
public class AdminOrderResponse {

    private String orderId;
    private String orderName;
    private String buyerName;
    private BigDecimal amount;
    private String status;
    private LocalDateTime updatedAt;

    public static AdminOrderResponse from(OrderEntity order) {
        AdminOrderResponse response = new AdminOrderResponse();
        response.orderId = order.getOrderId();
        response.orderName = order.getOrderName();
        response.buyerName = order.getBuyerName();
        response.amount = order.getAmount();
        response.status = order.getStatus().name();
        response.updatedAt = order.getUpdatedAt();
        return response;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderName() {
        return orderName;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
