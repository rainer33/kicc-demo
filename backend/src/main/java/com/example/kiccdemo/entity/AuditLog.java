package com.example.kiccdemo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
/**
 * 주요 보안/결제 이벤트를 추적하기 위한 감사 로그 엔티티입니다.
 */
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String action;

    @Column(nullable = false, length = 80)
    private String targetId;

    @Column(nullable = false, length = 600)
    private String detail;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static AuditLog of(String action, String targetId, String detail) {
        AuditLog log = new AuditLog();
        log.action = action;
        log.targetId = targetId;
        log.detail = detail;
        return log;
    }

    public Long getId() {
        return id;
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

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
