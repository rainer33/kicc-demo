package com.example.kiccdemo.service;

import com.example.kiccdemo.entity.OrderEntity;
import com.example.kiccdemo.entity.OrderStatus;
import com.example.kiccdemo.entity.Payment;
import com.example.kiccdemo.entity.PaymentStatus;
import com.example.kiccdemo.repository.OrderRepository;
import com.example.kiccdemo.repository.PaymentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 정기 보정 스케줄러 서비스입니다. 장시간 READY 결제 실패 처리 및 주문-결제 불일치를 복구합니다.
 *
 * 운영 관점:
 * - 트래픽 급증/장애 시 발생할 수 있는 \"중간 상태\"를 주기적으로 정리합니다.
 * - 사람이 수동 확인하기 전에 자동으로 데이터 정합성을 회복하도록 설계했습니다.
 */
@Service
public class ReconciliationService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final AuditService auditService;

    public ReconciliationService(PaymentRepository paymentRepository, OrderRepository orderRepository, AuditService auditService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.auditService = auditService;
    }

    /** 스케줄러 진입점: 장시간 READY 결제 자동 실패 처리 */
    @Scheduled(fixedDelayString = "${app.payment.reconcile-delay-ms:300000}")
    @Transactional
    public void compensateStuckReadyPayments() {
        runCompensateStuckReadyPayments();
    }

    /** 수동 실행/테스트에서도 재사용 가능한 READY 보정 로직 */
    @Transactional
    public int runCompensateStuckReadyPayments() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        List<Payment> stuck = paymentRepository.findByStatusAndUpdatedAtBefore(PaymentStatus.READY, threshold);
        for (Payment payment : stuck) {
            payment.markFailed("TIMEOUT: payment not completed in 10 minutes");
            orderRepository.findByOrderId(payment.getOrderId())
                    .ifPresent(order -> order.setStatus(OrderStatus.FAILED));
            auditService.log("AUTO_FAIL_READY", payment.getOrderId(), "Marked as FAILED by scheduler");
        }
        return stuck.size();
    }

    /** 스케줄러 진입점: 주문-결제 상태 불일치 복구 */
    @Scheduled(fixedDelayString = "${app.payment.reconcile-delay-ms:300000}")
    @Transactional
    public void repairOrderPaymentMismatch() {
        runRepairOrderPaymentMismatch();
    }

    /** 수동 실행/테스트에서도 재사용 가능한 주문-결제 정합성 복구 로직 */
    @Transactional
    public int runRepairOrderPaymentMismatch() {
        List<OrderEntity> pending = orderRepository.findTop100ByStatusOrderByUpdatedAtAsc(OrderStatus.PAYMENT_PENDING);
        int updated = 0;
        for (OrderEntity order : pending) {
            paymentRepository.findByOrderId(order.getOrderId()).ifPresent(payment -> {
                if (payment.getStatus() == PaymentStatus.APPROVED && order.getStatus() != OrderStatus.PAID) {
                    order.setStatus(OrderStatus.PAID);
                    auditService.log("AUTO_REPAIR_ORDER", order.getOrderId(), "Order moved to PAID from PAYMENT_PENDING");
                }
            });
            if (order.getStatus() == OrderStatus.PAID) {
                updated++;
            }
        }
        return updated;
    }
}
