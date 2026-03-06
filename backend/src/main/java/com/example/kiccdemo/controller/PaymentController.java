package com.example.kiccdemo.controller;

import com.example.kiccdemo.config.AppProperties;
import com.example.kiccdemo.dto.PaymentRefundRequest;
import com.example.kiccdemo.dto.PaymentReadyRequest;
import com.example.kiccdemo.dto.PaymentReadyResponse;
import com.example.kiccdemo.dto.PaymentResultResponse;
import com.example.kiccdemo.dto.RefundHistoryResponse;
import com.example.kiccdemo.entity.Payment;
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

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final AppProperties appProperties;
    private final KiccWebhookVerifier kiccWebhookVerifier;

    public PaymentController(PaymentService paymentService, AppProperties appProperties, KiccWebhookVerifier kiccWebhookVerifier) {
        this.paymentService = paymentService;
        this.appProperties = appProperties;
        this.kiccWebhookVerifier = kiccWebhookVerifier;
    }

    @PostMapping("/ready")
    public ResponseEntity<PaymentReadyResponse> ready(
            @Valid @RequestBody PaymentReadyRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return ResponseEntity.ok(paymentService.ready(request, idempotencyKey));
    }

    @PostMapping("/{orderId}/mock-approve")
    public ResponseEntity<PaymentResultResponse> mockApprove(@PathVariable String orderId) {
        validateMockMode();
        Payment payment = paymentService.mockApprove(orderId);
        return ResponseEntity.ok(PaymentResultResponse.from(payment));
    }

    @PostMapping("/{orderId}/mock-cancel")
    public ResponseEntity<PaymentResultResponse> mockCancel(@PathVariable String orderId) {
        validateMockMode();
        Payment payment = paymentService.mockCancel(orderId);
        return ResponseEntity.ok(PaymentResultResponse.from(payment));
    }

    @PostMapping("/{orderId}/mock-refund")
    public ResponseEntity<PaymentResultResponse> mockRefund(
            @PathVariable String orderId,
            @Valid @RequestBody PaymentRefundRequest request
    ) {
        validateMockMode();
        Payment payment = paymentService.mockRefund(orderId, request.getAmount(), request.getReason());
        return ResponseEntity.ok(PaymentResultResponse.from(payment));
    }

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

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResultResponse> get(@PathVariable String orderId) {
        Payment payment = paymentService.getPayment(orderId);
        return ResponseEntity.ok(PaymentResultResponse.from(payment));
    }

    @GetMapping("/{orderId}/refund-history")
    public ResponseEntity<List<RefundHistoryResponse>> refundHistory(@PathVariable String orderId) {
        List<RefundHistoryResponse> response = paymentService.getRefundHistory(orderId)
                .stream()
                .map(RefundHistoryResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    private void validateMockMode() {
        if (!appProperties.getPayment().getKicc().isUseMockApprove()) {
            throw new IllegalArgumentException("Mock approve mode is disabled");
        }
    }
}
