package com.example.kiccdemo.repository;

import com.example.kiccdemo.entity.PaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 환불 엔티티 조회/저장 리포지토리입니다. 결제별 환불 이력 조회를 지원합니다.
 */
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {
    List<PaymentRefund> findByPaymentOrderByCreatedAtDesc(com.example.kiccdemo.entity.Payment payment);
}
