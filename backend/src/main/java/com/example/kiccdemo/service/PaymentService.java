package com.example.kiccdemo.service;

import com.example.kiccdemo.config.AppProperties;
import com.example.kiccdemo.dto.PaymentReadyRequest;
import com.example.kiccdemo.dto.PaymentReadyResponse;
import com.example.kiccdemo.entity.*;
import com.example.kiccdemo.repository.IdempotencyRecordRepository;
import com.example.kiccdemo.repository.OrderRepository;
import com.example.kiccdemo.repository.PaymentRefundRepository;
import com.example.kiccdemo.repository.PaymentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 결제 핵심 비즈니스 서비스입니다. 주문/결제 상태 전이, 멱등 처리, 환불, 콜백을 통합 관리합니다.
 *
 * 설계 의도:
 * - 주문(Order)과 결제(Payment)를 분리해 각각의 상태 머신을 명확히 유지합니다.
 * - 멱등키(Idempotency-Key)를 통해 동일 요청의 중복 생성/중복 과금을 방지합니다.
 * - 콜백 재전송/지연 상황에서도 이미 확정된 상태를 덮어쓰지 않도록 방어합니다.
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final OrderRepository orderRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final IdempotencyService idempotencyService;
    private final RedisDedupService redisDedupService;
    private final KiccPayloadFactory kiccPayloadFactory;
    private final AppProperties appProperties;
    private final AuditService auditService;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentRefundRepository paymentRefundRepository,
            OrderRepository orderRepository,
            IdempotencyRecordRepository idempotencyRecordRepository,
            IdempotencyService idempotencyService,
            RedisDedupService redisDedupService,
            KiccPayloadFactory kiccPayloadFactory,
            AppProperties appProperties,
            AuditService auditService
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentRefundRepository = paymentRefundRepository;
        this.orderRepository = orderRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.idempotencyService = idempotencyService;
        this.redisDedupService = redisDedupService;
        this.kiccPayloadFactory = kiccPayloadFactory;
        this.appProperties = appProperties;
        this.auditService = auditService;
    }

    /**
     * 결제 준비(ready) 요청을 처리합니다.
     *
     * 처리 순서:
     * 1) 요청 바디를 해시해 멱등 비교 기준을 만듭니다.
     * 2) 멱등키가 있으면 별도 트랜잭션에서 예약/조회하여 중복을 흡수합니다.
     * 3) 신규 요청이면 주문/결제를 생성하고 멱등 레코드와 연결합니다.
     * 4) 프론트가 PG로 보낼 폼 필드를 생성해 응답합니다.
     */
    @Transactional
    public PaymentReadyResponse ready(PaymentReadyRequest request, String idempotencyKey) {
        String requestHash = buildRequestHash(request);
        String normalizedIdemKey = normalizeIdemKey(idempotencyKey);
        boolean redisLockAcquired = false;

        if (normalizedIdemKey != null) {
            // 1) Redis 결과 캐시가 있으면 DB 조회 후 즉시 반환
            Optional<String> cachedOrderId = redisDedupService.getResultOrderId(normalizedIdemKey);
            if (cachedOrderId.isPresent()) {
                return buildReadyResponse(findByOrderId(cachedOrderId.get()));
            }
            // 2) Redis NX lock으로 동일 키 동시 요청 선점
            redisLockAcquired = redisDedupService.tryAcquireLock(normalizedIdemKey, Duration.ofSeconds(30));
            if (!redisLockAcquired) {
                throw new IllegalStateException("Idempotent request is in progress. Retry shortly.");
            }
        }

        IdempotencyRecord reserved = null;
        try {
            if (normalizedIdemKey != null) {
                IdempotencyService.Reservation reservation = idempotencyService.reserve(normalizedIdemKey, requestHash);
                reserved = reservation.record();
                if (reservation.existing()) {
                    // 기존 처리 완료 요청이면 기존 orderId를 재사용해 같은 응답 성격을 유지합니다.
                    if (reserved.getOrderId() == null
                            || reserved.getOrderId().isBlank()
                            || "__PENDING__".equals(reserved.getOrderId())) {
                        // 다른 요청이 아직 처리 중인 중간 상태입니다.
                        throw new IllegalStateException("Idempotent request is in progress. Retry shortly.");
                    }
                    return buildReadyResponse(findByOrderId(reserved.getOrderId()));
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

            if (reserved != null) {
                // 별도 트랜잭션에서 예약한 멱등 레코드에 최종 orderId를 연결합니다.
                reserved.setOrderId(saved.getOrderId());
                idempotencyRecordRepository.save(reserved);
            }

            auditService.log("PAYMENT_READY", saved.getOrderId(), "Payment and order created");
            if (normalizedIdemKey != null) {
                // 결과 캐시에 orderId를 저장해 재요청 시 빠르게 응답합니다.
                redisDedupService.saveResult(normalizedIdemKey, saved.getOrderId(), Duration.ofHours(24));
            }
            return buildReadyResponse(saved);
        } finally {
            if (normalizedIdemKey != null && redisLockAcquired) {
                redisDedupService.releaseLock(normalizedIdemKey);
            }
        }
    }

    /** READY 상태 결제를 mock 승인 처리합니다. */
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

    /** APPROVED 상태 결제를 mock 전체취소 처리합니다. */
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

    /**
     * 결제 환불(mock)을 처리합니다.
     *
     * - APPROVED 또는 PARTIALLY_REFUNDED 상태만 허용
     * - 환불 가능 금액을 초과하면 차단
     * - 환불 이력을 별도 엔티티로 저장
     */
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

    /**
     * KICC 콜백을 처리합니다.
     *
     * 보안/무결성 정책:
     * - 이미 취소/환불 확정된 결제는 콜백으로 상태를 변경하지 않습니다.
     * - APPROVED 상태에서 중복 성공 콜백은 무시합니다.
     * - APPROVED 상태에서 txid 불일치가 들어오면 위변조로 보고 차단합니다.
     */
    @Transactional
    public Payment handleKiccCallback(String orderId, String resultCode, String resultMsg, String tXid) {
        Payment payment = findByOrderId(orderId);
        if (payment.getStatus() == PaymentStatus.CANCELED
                || payment.getStatus() == PaymentStatus.PARTIALLY_REFUNDED
                || payment.getStatus() == PaymentStatus.REFUNDED) {
            auditService.log("KICC_CALLBACK_IGNORED", orderId, "Ignored callback for finalized state=" + payment.getStatus());
            return payment;
        }

        if (payment.getStatus() == PaymentStatus.APPROVED) {
            if ("0000".equals(resultCode)) {
                if (tXid != null && !tXid.isBlank() && payment.getPgTransactionId() != null
                        && !Objects.equals(payment.getPgTransactionId(), tXid)) {
                    throw new SecurityException("Callback txid mismatch");
                }
                auditService.log("KICC_CALLBACK_DUPLICATE", orderId, "Duplicate approved callback ignored");
                return payment;
            }
            auditService.log("KICC_CALLBACK_IGNORED", orderId, "Failure callback ignored for APPROVED state");
            return payment;
        }

        if (payment.getStatus() != PaymentStatus.READY) {
            auditService.log("KICC_CALLBACK_IGNORED", orderId, "Ignored callback for state=" + payment.getStatus());
            return payment;
        }

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

    /** 단건 결제 상태 조회 */
    @Transactional(readOnly = true)
    public Payment getPayment(String orderId) {
        return findByOrderId(orderId);
    }

    /** 결제 기준 환불 이력 조회 */
    @Transactional(readOnly = true)
    public List<PaymentRefund> getRefundHistory(String orderId) {
        Payment payment = findByOrderId(orderId);
        return paymentRefundRepository.findByPaymentOrderByCreatedAtDesc(payment);
    }

    /** 관리자 결제 목록 조회(최대 100건) */
    @Transactional(readOnly = true)
    public List<Payment> adminPayments(PaymentStatus status) {
        PageRequest pageRequest = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "updatedAt"));
        if (status == null) {
            return paymentRepository.findAllByOrderByUpdatedAtDesc(pageRequest).getContent();
        }
        return paymentRepository.findByStatusOrderByUpdatedAtDesc(status, pageRequest).getContent();
    }

    /** 관리자 주문 목록 조회(기간 + 최대 100건) */
    @Transactional(readOnly = true)
    public List<OrderEntity> adminOrders(LocalDateTime from, LocalDateTime to) {
        LocalDateTime fromTime = from == null ? LocalDateTime.now().minusDays(7) : from;
        LocalDateTime toTime = to == null ? LocalDateTime.now().plusSeconds(1) : to;
        PageRequest pageRequest = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return orderRepository.findByUpdatedAtBetweenOrderByUpdatedAtDesc(fromTime, toTime, pageRequest).getContent();
    }

    /** 결제 상태를 기준으로 주문 상태를 일관되게 동기화합니다. */
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

    /** orderId로 결제를 조회하고 없으면 명시적으로 예외를 발생시킵니다. */
    private Payment findByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for orderId=" + orderId));
    }

    /** 외부 노출 가능한 주문번호를 생성합니다. */
    private String generateOrderId() {
        return "ORDER-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    /** 멱등 요청 동일성 비교를 위한 SHA-256 해시를 생성합니다. */
    private String buildRequestHash(PaymentReadyRequest request) {
        try {
            String payload = request.getOrderName() + "|" + request.getBuyerName() + "|" + request.getAmount().toPlainString();
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build request hash", e);
        }
    }

    /** 공백/NULL을 정규화해 멱등키 사용 여부를 일관되게 판단합니다. */
    private String normalizeIdemKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return key.trim();
    }

    /** 결제 준비 응답 DTO를 생성합니다. */
    private PaymentReadyResponse buildReadyResponse(Payment payment) {
        Map<String, String> fields = kiccPayloadFactory.createFormFields(payment);
        return new PaymentReadyResponse(
                payment.getOrderId(),
                appProperties.getPayment().getKicc().getPayUrl(),
                appProperties.getPayment().getKicc().isUseMockApprove(),
                fields
        );
    }
}
