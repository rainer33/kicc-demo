package com.example.kiccdemo.repository;

import com.example.kiccdemo.entity.OrderEntity;
import com.example.kiccdemo.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 주문 엔티티 조회/저장 리포지토리입니다. 상태 기반 조회와 기간 조회를 지원합니다.
 */
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderId(String orderId);

    List<OrderEntity> findTop100ByStatusOrderByUpdatedAtAsc(OrderStatus status);

    Page<OrderEntity> findByUpdatedAtBetweenOrderByUpdatedAtDesc(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
