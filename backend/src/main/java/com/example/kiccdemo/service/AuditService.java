package com.example.kiccdemo.service;

import com.example.kiccdemo.entity.AuditLog;
import com.example.kiccdemo.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
