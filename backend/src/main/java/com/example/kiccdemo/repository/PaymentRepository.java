package com.example.kiccdemo.repository;

import com.example.kiccdemo.entity.Payment;
import com.example.kiccdemo.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);

    Page<Payment> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    Page<Payment> findByStatusOrderByUpdatedAtDesc(PaymentStatus status, Pageable pageable);

    List<Payment> findByStatusAndUpdatedAtBefore(PaymentStatus status, LocalDateTime updatedAt);
}
