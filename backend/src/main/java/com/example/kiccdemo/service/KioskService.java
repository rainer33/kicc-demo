package com.example.kiccdemo.service;

import com.example.kiccdemo.config.AppProperties;
import com.example.kiccdemo.dto.KioskPaymentRequest;
import com.example.kiccdemo.dto.KioskSessionCreateRequest;
import com.example.kiccdemo.dto.KioskSessionResponse;
import com.example.kiccdemo.dto.KioskTerminalResultRequest;
import com.example.kiccdemo.dto.PaymentReadyRequest;
import com.example.kiccdemo.dto.PaymentReadyResponse;
import com.example.kiccdemo.entity.KioskPaymentMethod;
import com.example.kiccdemo.entity.KioskSession;
import com.example.kiccdemo.entity.KioskSessionStatus;
import com.example.kiccdemo.exception.ResourceNotFoundException;
import com.example.kiccdemo.repository.KioskSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 키오스크 결제 데모 서비스입니다.
 *
 * 흐름:
 * 1) 세션 생성
 * 2) 결제요청(ready)
 * 3) 단말 승인/실패 이벤트 반영
 */
@Service
public class KioskService {

    private final KioskSessionRepository kioskSessionRepository;
    private final PaymentService paymentService;
    private final AppProperties appProperties;
    private final AuditService auditService;

    public KioskService(
            KioskSessionRepository kioskSessionRepository,
            PaymentService paymentService,
            AppProperties appProperties,
            AuditService auditService
    ) {
        this.kioskSessionRepository = kioskSessionRepository;
        this.paymentService = paymentService;
        this.appProperties = appProperties;
        this.auditService = auditService;
    }

    /**
     * 키오스크 세션을 생성합니다.
     */
    @Transactional
    public KioskSessionResponse createSession(KioskSessionCreateRequest request) {
        KioskSession session = new KioskSession();
        session.setSessionId("KS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase());
        session.setKioskId(request.getKioskId().trim());
        session.setOrderName(request.getOrderName().trim());
        session.setAmount(request.getAmount());
        session.setStatus(KioskSessionStatus.CREATED);
        session.setLastMessage("주문 생성 완료");

        KioskSession saved = kioskSessionRepository.save(session);
        auditService.log("KIOSK_SESSION_CREATED", saved.getSessionId(), "kioskId=" + saved.getKioskId());
        return KioskSessionResponse.from(saved);
    }

    /**
     * 키오스크 결제를 요청합니다.
     */
    @Transactional
    public KioskSessionResponse requestPayment(String sessionId, KioskPaymentRequest request) {
        KioskSession session = findBySessionId(sessionId);
        if (session.getStatus() != KioskSessionStatus.CREATED && session.getStatus() != KioskSessionStatus.FAILED) {
            throw new IllegalStateException("Payment request is not allowed in current state=" + session.getStatus());
        }

        validateMockMode();

        session.setPaymentMethod(request.getPaymentMethod());

        PaymentReadyRequest readyRequest = new PaymentReadyRequest();
        readyRequest.setOrderName("[KIOSK] " + session.getOrderName());
        readyRequest.setBuyerName("KIOSK-" + session.getKioskId());
        readyRequest.setAmount(session.getAmount());

        // 키오스크 플로우는 세션 상태머신(CREATED -> PAYMENT_REQUESTED)으로 중복 요청을 제어합니다.
        // 따라서 별도 Idempotency-Key를 전달하지 않고 결제 준비를 진행합니다.
        PaymentReadyResponse readyResponse = paymentService.ready(readyRequest, null);
        session.setOrderId(readyResponse.getOrderId());

        if (request.getPaymentMethod() == KioskPaymentMethod.CASH) {
            paymentService.mockApprove(readyResponse.getOrderId());
            session.setStatus(KioskSessionStatus.APPROVED);
            session.setTerminalTransactionId("CASH-" + UUID.randomUUID().toString().substring(0, 8));
            session.setLastMessage("현금 결제 승인 완료");
            auditService.log("KIOSK_CASH_APPROVED", session.getSessionId(), "orderId=" + readyResponse.getOrderId());
        } else {
            session.setStatus(KioskSessionStatus.PAYMENT_REQUESTED);
            session.setLastMessage("단말 승인 대기 중");
            auditService.log("KIOSK_PAYMENT_REQUESTED", session.getSessionId(), "orderId=" + readyResponse.getOrderId());
        }

        return KioskSessionResponse.from(session);
    }

    /**
     * 단말 승인 이벤트를 반영합니다.
     */
    @Transactional
    public KioskSessionResponse approveByTerminal(String sessionId, KioskTerminalResultRequest request) {
        KioskSession session = findBySessionId(sessionId);
        if (session.getStatus() != KioskSessionStatus.PAYMENT_REQUESTED) {
            throw new IllegalStateException("Terminal approve is not allowed in current state=" + session.getStatus());
        }
        if (session.getOrderId() == null || session.getOrderId().isBlank()) {
            throw new IllegalStateException("OrderId is missing for kiosk session");
        }

        paymentService.mockApprove(session.getOrderId());
        session.setStatus(KioskSessionStatus.APPROVED);
        session.setTerminalTransactionId(request.getTerminalTransactionId());
        session.setLastMessage(request.getMessage() == null || request.getMessage().isBlank()
                ? "단말 승인 완료"
                : request.getMessage());
        auditService.log("KIOSK_TERMINAL_APPROVED", session.getSessionId(), "orderId=" + session.getOrderId());
        return KioskSessionResponse.from(session);
    }

    /**
     * 단말 실패 이벤트를 반영합니다.
     */
    @Transactional
    public KioskSessionResponse failByTerminal(String sessionId, KioskTerminalResultRequest request) {
        KioskSession session = findBySessionId(sessionId);
        if (session.getStatus() != KioskSessionStatus.PAYMENT_REQUESTED) {
            throw new IllegalStateException("Terminal fail is not allowed in current state=" + session.getStatus());
        }
        if (session.getOrderId() == null || session.getOrderId().isBlank()) {
            throw new IllegalStateException("OrderId is missing for kiosk session");
        }

        String message = request.getMessage() == null || request.getMessage().isBlank()
                ? "단말 승인 거절"
                : request.getMessage();

        paymentService.handleKiccCallback(session.getOrderId(), "9999", message, request.getTerminalTransactionId());

        session.setStatus(KioskSessionStatus.FAILED);
        session.setTerminalTransactionId(request.getTerminalTransactionId());
        session.setLastMessage(message);
        auditService.log("KIOSK_TERMINAL_FAILED", session.getSessionId(), "orderId=" + session.getOrderId());
        return KioskSessionResponse.from(session);
    }

    /**
     * 세션 단건 조회입니다.
     */
    @Transactional(readOnly = true)
    public KioskSessionResponse get(String sessionId) {
        return KioskSessionResponse.from(findBySessionId(sessionId));
    }

    /**
     * 최신 세션 목록 조회입니다.
     */
    @Transactional(readOnly = true)
    public List<KioskSessionResponse> latest() {
        return kioskSessionRepository.findTop50ByOrderByUpdatedAtDesc()
                .stream()
                .map(KioskSessionResponse::from)
                .toList();
    }

    private KioskSession findBySessionId(String sessionId) {
        return kioskSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Kiosk session not found: " + sessionId));
    }

    private void validateMockMode() {
        if (!appProperties.getPayment().getKicc().isUseMockApprove()) {
            throw new IllegalArgumentException("Kiosk demo requires mock approve mode");
        }
    }
}
