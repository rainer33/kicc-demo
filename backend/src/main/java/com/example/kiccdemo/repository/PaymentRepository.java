package com.example.kiccdemo.repository;

import com.example.kiccdemo.entity.Payment;
import com.example.kiccdemo.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 결제 엔티티 조회/저장 리포지토리입니다. 상태/시간 기준 조회와 페이지 조회를 지원합니다.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);

    Page<Payment> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    Page<Payment> findByStatusOrderByUpdatedAtDesc(PaymentStatus status, Pageable pageable);

    List<Payment> findByStatusAndUpdatedAtBefore(PaymentStatus status, LocalDateTime updatedAt);
}
