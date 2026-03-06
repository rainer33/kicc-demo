package com.example.kiccdemo.repository;

import com.example.kiccdemo.entity.Payment;
import com.example.kiccdemo.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findTop100ByOrderByUpdatedAtDesc();

    List<Payment> findByStatusOrderByUpdatedAtDesc(PaymentStatus status);

    List<Payment> findByStatusAndUpdatedAtBefore(PaymentStatus status, LocalDateTime updatedAt);
}
