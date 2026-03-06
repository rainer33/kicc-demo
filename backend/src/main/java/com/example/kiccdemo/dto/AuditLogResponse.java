package com.example.kiccdemo.dto;

import com.example.kiccdemo.entity.AuditLog;

import java.time.LocalDateTime;

public class AuditLogResponse {

    private String action;
    private String targetId;
    private String detail;
    private LocalDateTime createdAt;

    public static AuditLogResponse from(AuditLog log) {
        AuditLogResponse response = new AuditLogResponse();
        response.action = log.getAction();
        response.targetId = log.getTargetId();
        response.detail = log.getDetail();
        response.createdAt = log.getCreatedAt();
        return response;
    }

    public String getAction() {
        return action;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getDetail() {
        return detail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
