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

    @Scheduled(fixedDelayString = "${app.payment.reconcile-delay-ms:300000}")
    @Transactional
    public void compensateStuckReadyPayments() {
        runCompensateStuckReadyPayments();
    }

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

    @Scheduled(fixedDelayString = "${app.payment.reconcile-delay-ms:300000}")
    @Transactional
    public void repairOrderPaymentMismatch() {
        runRepairOrderPaymentMismatch();
    }

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
