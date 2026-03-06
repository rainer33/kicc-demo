package com.example.kiccdemo.repository;

import com.example.kiccdemo.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 감사 로그 엔티티의 조회/저장을 담당하는 JPA 리포지토리입니다.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop200ByOrderByCreatedAtDesc();
}
