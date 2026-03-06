package com.example.kiccdemo.repository;

import com.example.kiccdemo.entity.PaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {
    List<PaymentRefund> findByPaymentOrderByCreatedAtDesc(com.example.kiccdemo.entity.Payment payment);
}
