package com.example.kiccdemo.repository;

import com.example.kiccdemo.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 멱등키 레코드를 조회/저장하는 JPA 리포지토리입니다.
 */
public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {
    Optional<IdempotencyRecord> findByIdemKey(String idemKey);
}
