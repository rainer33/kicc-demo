package com.example.kiccdemo.service;

import com.example.kiccdemo.entity.AuditLog;
import com.example.kiccdemo.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/**
 * 감사 로그 기록/조회 서비스입니다. 핵심 이벤트를 중앙에서 남기고 검색합니다.
 */
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String targetId, String detail) {
        auditLogRepository.save(AuditLog.of(action, targetId, detail));
    }

    public List<AuditLog> latest() {
        return auditLogRepository.findTop200ByOrderByCreatedAtDesc();
    }
}
