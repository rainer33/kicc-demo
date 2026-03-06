package com.example.kiccdemo.repository;

import com.example.kiccdemo.entity.KioskSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 키오스크 세션 리포지토리입니다.
 */
public interface KioskSessionRepository extends JpaRepository<KioskSession, Long> {
    Optional<KioskSession> findBySessionId(String sessionId);

    List<KioskSession> findTop50ByOrderByUpdatedAtDesc();
}
