package com.example.kiccdemo.repository;

import com.example.kiccdemo.entity.OrderEntity;
import com.example.kiccdemo.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderId(String orderId);

    List<OrderEntity> findTop100ByStatusOrderByUpdatedAtAsc(OrderStatus status);

    Page<OrderEntity> findByUpdatedAtBetweenOrderByUpdatedAtDesc(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
