package com.example.kiccdemo.service;

import com.example.kiccdemo.config.AppProperties;
import com.example.kiccdemo.dto.PaymentReadyRequest;
import com.example.kiccdemo.dto.PaymentReadyResponse;
import com.example.kiccdemo.entity.*;
import com.example.kiccdemo.repository.IdempotencyRecordRepository;
import com.example.kiccdemo.repository.OrderRepository;
import com.example.kiccdemo.repository.PaymentRefundRepository;
import com.example.kiccdemo.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final OrderRepository orderRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final KiccPayloadFactory kiccPayloadFactory;
    private final AppProperties appProperties;
    private final AuditService auditService;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentRefundRepository paymentRefundRepository,
            OrderRepository orderRepository,
            IdempotencyRecordRepository idempotencyRecordRepository,
            KiccPayloadFactory kiccPayloadFactory,
            AppProperties appProperties,
            AuditService auditService
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentRefundRepository = paymentRefundRepository;
        this.orderRepository = orderRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.kiccPayloadFactory = kiccPayloadFactory;
        this.appProperties = appProperties;
        this.auditService = auditService;
    }

    @Transactional
    public PaymentReadyResponse ready(PaymentReadyRequest request, String idempotencyKey) {
        String requestHash = buildRequestHash(request);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            IdempotencyRecord record = idempotencyRecordRepository.findByIdemKey(idempotencyKey).orElse(null);
            if (record != null) {
                if (!record.getRequestHash().equals(requestHash)) {
                    throw new IllegalArgumentException("Idempotency key already used with different request");
                }
                Payment existing = findByOrderId(record.getOrderId());
                Map<String, String> fields = kiccPayloadFactory.createFormFields(existing);
                return new PaymentReadyResponse(
                        existing.getOrderId(),
                        appProperties.getPayment().getKicc().getPayUrl(),
                        appProperties.getPayment().getKicc().isUseMockApprove(),
                        fields
                );
            }
        }

        String orderId = generateOrderId();
        OrderEntity order = new OrderEntity();
        order.setOrderId(orderId);
        order.setOrderName(request.getOrderName());
        order.setBuyerName(request.getBuyerName());
        order.setAmount(request.getAmount());
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        orderRepository.save(order);

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setOrderName(request.getOrderName());
        payment.setBuyerName(request.getBuyerName());
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.READY);

        Payment saved = paymentRepository.save(payment);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            IdempotencyRecord record = new IdempotencyRecord();
            record.setIdemKey(idempotencyKey);
            record.setOrderId(saved.getOrderId());
            record.setRequestHash(requestHash);
            idempotencyRecordRepository.save(record);
        }

        auditService.log("PAYMENT_READY", saved.getOrderId(), "Payment and order created");

        Map<String, String> fields = kiccPayloadFactory.createFormFields(saved);
        return new PaymentReadyResponse(
                saved.getOrderId(),
                appProperties.getPayment().getKicc().getPayUrl(),
                appProperties.getPayment().getKicc().isUseMockApprove(),
                fields
        );
    }

    @Transactional
    public Payment mockApprove(String orderId) {
        Payment payment = findByOrderId(orderId);
        if (payment.getStatus() != PaymentStatus.READY) {
            throw new IllegalArgumentException("Only READY payment can be approved");
        }
        payment.markApproved("MOCK-" + UUID.randomUUID().toString().substring(0, 8));
        syncOrderStatus(payment);
        auditService.log("MOCK_APPROVE", orderId, "Payment approved");
        return payment;
    }

    @Transactional
    public Payment mockCancel(String orderId) {
        Payment payment = findByOrderId(orderId);
        if (payment.getStatus() != PaymentStatus.APPROVED) {
            throw new IllegalArgumentException("Only APPROVED payment can be canceled");
        }
        payment.markCanceled();
        syncOrderStatus(payment);
        auditService.log("MOCK_CANCEL", orderId, "Payment canceled");
        return payment;
    }

    @Transactional
    public Payment mockRefund(String orderId, BigDecimal amount, String reason) {
        Payment payment = findByOrderId(orderId);
        if (payment.getStatus() != PaymentStatus.APPROVED && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new IllegalArgumentException("Only APPROVED or PARTIALLY_REFUNDED payment can be refunded");
        }
        BigDecimal refundable = payment.getAmount().subtract(payment.getRefundedAmount());
        if (amount.compareTo(refundable) > 0) {
            throw new IllegalArgumentException("Refund amount exceeds refundable amount");
        }

        payment.addRefund(amount);

        PaymentRefund refund = new PaymentRefund();
        refund.setPayment(payment);
        refund.setAmount(amount);
        refund.setReason(reason);
        refund.setRefundTransactionId("MOCK-RFD-" + UUID.randomUUID().toString().substring(0, 8));
        paymentRefundRepository.save(refund);

        syncOrderStatus(payment);
        auditService.log("MOCK_REFUND", orderId, "Refunded amount=" + amount);
        return payment;
    }

    @Transactional
    public Payment handleKiccCallback(String orderId, String resultCode, String resultMsg, String tXid) {
        Payment payment = findByOrderId(orderId);
        if ("0000".equals(resultCode)) {
            payment.markApproved(tXid == null || tXid.isBlank() ? "UNKNOWN-TXID" : tXid);
            auditService.log("KICC_APPROVED", orderId, "Callback approved");
        } else {
            payment.markFailed(resultCode + ": " + (resultMsg == null ? "Unknown error" : resultMsg));
            auditService.log("KICC_FAILED", orderId, "Callback failed code=" + resultCode);
        }
        syncOrderStatus(payment);
        return payment;
    }

    @Transactional(readOnly = true)
    public Payment getPayment(String orderId) {
        return findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<PaymentRefund> getRefundHistory(String orderId) {
        Payment payment = findByOrderId(orderId);
        return paymentRefundRepository.findByPaymentOrderByCreatedAtDesc(payment);
    }

    @Transactional(readOnly = true)
    public List<Payment> adminPayments(PaymentStatus status) {
        if (status == null) {
            return paymentRepository.findTop100ByOrderByUpdatedAtDesc();
        }
        return paymentRepository.findByStatusOrderByUpdatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> adminOrders(LocalDateTime from, LocalDateTime to) {
        LocalDateTime fromTime = from == null ? LocalDateTime.now().minusDays(7) : from;
        LocalDateTime toTime = to == null ? LocalDateTime.now().plusSeconds(1) : to;
        return orderRepository.findByUpdatedAtBetweenOrderByUpdatedAtDesc(fromTime, toTime);
    }

    private void syncOrderStatus(Payment payment) {
        OrderEntity order = orderRepository.findByOrderId(payment.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found for orderId=" + payment.getOrderId()));

        switch (payment.getStatus()) {
            case READY -> order.setStatus(OrderStatus.PAYMENT_PENDING);
            case APPROVED -> order.setStatus(OrderStatus.PAID);
            case CANCELED -> order.setStatus(OrderStatus.CANCELED);
            case PARTIALLY_REFUNDED -> order.setStatus(OrderStatus.PARTIALLY_REFUNDED);
            case REFUNDED -> order.setStatus(OrderStatus.REFUNDED);
            case FAILED -> order.setStatus(OrderStatus.FAILED);
        }
    }

    private Payment findByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for orderId=" + orderId));
    }

    private String generateOrderId() {
        return "ORDER-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    private String buildRequestHash(PaymentReadyRequest request) {
        try {
            String payload = request.getOrderName() + "|" + request.getBuyerName() + "|" + request.getAmount().toPlainString();
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build request hash", e);
        }
    }
}
