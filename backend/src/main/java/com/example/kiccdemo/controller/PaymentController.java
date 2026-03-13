package com.example.kiccdemo.controller;

import com.example.kiccdemo.config.AppProperties;
import com.example.kiccdemo.dto.PaymentRefundRequest;
import com.example.kiccdemo.dto.PaymentReadyRequest;
import com.example.kiccdemo.dto.PaymentReadyResponse;
import com.example.kiccdemo.dto.PaymentResultResponse;
import com.example.kiccdemo.dto.RefundHistoryResponse;
import com.example.kiccdemo.entity.Payment;
import com.example.kiccdemo.service.AdminAuthService;
import com.example.kiccdemo.service.KiccWebhookVerifier;
import com.example.kiccdemo.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

/**
 * 결제 준비/승인/취소/환불/콜백/조회 API를 외부에 노출하는 컨트롤러입니다.
 *
 * 이 컨트롤러의 책임:
 * - HTTP 요청/응답 형태를 정의
 * - 입력 검증(@Valid) 적용
 * - 보안 검증(콜백 서명, mock 모드) 선행
 * - 실제 비즈니스 처리는 PaymentService에 위임
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final AppProperties appProperties;
    private final KiccWebhookVerifier kiccWebhookVerifier;
    private final AdminAuthService adminAuthService;

    public PaymentController(
            PaymentService paymentService,
            AppProperties appProperties,
            KiccWebhookVerifier kiccWebhookVerifier,
            AdminAuthService adminAuthService
    ) {
        this.paymentService = paymentService;
        this.appProperties = appProperties;
        this.kiccWebhookVerifier = kiccWebhookVerifier;
        this.adminAuthService = adminAuthService;
    }

    /** 결제 준비 API: 주문 정보를 받아 orderId와 PG 전송용 필드를 발급합니다. */
    @PostMapping("/ready")
    public ResponseEntity<PaymentReadyResponse> ready(
            @Valid @RequestBody PaymentReadyRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return ResponseEntity.ok(paymentService.ready(request, idempotencyKey));
    }

    /** mock 승인 API: 실가맹점이 없는 개발 단계에서 승인 흐름을 재현합니다. */
    @PostMapping("/{orderId}/mock-approve")
    public ResponseEntity<PaymentResultResponse> mockApprove(
            @PathVariable String orderId,
            @RequestHeader("X-Admin-Token") String adminToken
    ) {
        adminAuthService.validateToken(adminToken);
        validateMockMode();
        Payment payment = paymentService.mockApprove(orderId);
        return ResponseEntity.ok(PaymentResultResponse.from(payment));
    }

    /** mock 전체취소 API */
    @PostMapping("/{orderId}/mock-cancel")
    public ResponseEntity<PaymentResultResponse> mockCancel(
            @PathVariable String orderId,
            @RequestHeader("X-Admin-Token") String adminToken
    ) {
        adminAuthService.validateToken(adminToken);
        validateMockMode();
        Payment payment = paymentService.mockCancel(orderId);
        return ResponseEntity.ok(PaymentResultResponse.from(payment));
    }

    /** mock 부분환불 API */
    @PostMapping("/{orderId}/mock-refund")
    public ResponseEntity<PaymentResultResponse> mockRefund(
            @PathVariable String orderId,
            @RequestHeader("X-Admin-Token") String adminToken,
            @Valid @RequestBody PaymentRefundRequest request
    ) {
        adminAuthService.validateToken(adminToken);
        validateMockMode();
        Payment payment = paymentService.mockRefund(orderId, request.getAmount(), request.getReason());
        return ResponseEntity.ok(PaymentResultResponse.from(payment));
    }

    /**
     * KICC 콜백 수신 API.
     *
     * - X-KICC-SIGNATURE 헤더 검증 후 처리
     * - 실제 상태 변경 로직은 PaymentService에서 안전하게 수행
     */
    @PostMapping("/kicc/callback")
    public ResponseEntity<Map<String, String>> callback(
            @RequestParam("oid") String orderId,
            @RequestParam("resCd") String resultCode,
            @RequestParam(value = "resMsg", required = false) String resultMsg,
            @RequestParam(value = "tXid", required = false) String tXid,
            @RequestHeader(value = "X-KICC-SIGNATURE", required = false) String signature
    ) {
        kiccWebhookVerifier.verify(orderId, resultCode, tXid, signature);
        paymentService.handleKiccCallback(orderId, resultCode, resultMsg, tXid);
        return ResponseEntity.ok(Map.of("result", "OK"));
    }

    /** 결제 단건 상태 조회 API */
    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResultResponse> get(@PathVariable String orderId) {
        Payment payment = paymentService.getPayment(orderId);
        return ResponseEntity.ok(PaymentResultResponse.from(payment));
    }

    /** 결제 기준 환불 이력 조회 API */
    @GetMapping("/{orderId}/refund-history")
    public ResponseEntity<List<RefundHistoryResponse>> refundHistory(@PathVariable String orderId) {
        List<RefundHistoryResponse> response = paymentService.getRefundHistory(orderId)
                .stream()
                .map(RefundHistoryResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /** mock 전용 API 호출 허용 여부를 점검합니다. */
    private void validateMockMode() {
        if (!appProperties.getPayment().getKicc().isUseMockApprove()) {
            throw new IllegalArgumentException("Mock approve mode is disabled");
        }
    }
}
