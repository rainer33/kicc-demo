package com.example.kiccdemo.controller;

import com.example.kiccdemo.dto.AdminOrderResponse;
import com.example.kiccdemo.dto.AuditLogResponse;
import com.example.kiccdemo.dto.PaymentResultResponse;
import com.example.kiccdemo.entity.PaymentStatus;
import com.example.kiccdemo.service.AdminAuthService;
import com.example.kiccdemo.service.AuditService;
import com.example.kiccdemo.service.PaymentService;
import com.example.kiccdemo.service.ReconciliationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 관리자 전용 조회/보정 API를 제공하는 컨트롤러입니다. 토큰 검증을 통과한 요청만 허용합니다.
 *
 * 운영 중 필요한 관측 포인트(결제/주문/감사로그)를 일괄 조회하고
 * 수동 보정(reconcile-now)을 통해 즉시 데이터 정합성 회복을 실행할 수 있습니다.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final PaymentService paymentService;
    private final AuditService auditService;
    private final AdminAuthService adminAuthService;
    private final ReconciliationService reconciliationService;

    public AdminController(
            PaymentService paymentService,
            AuditService auditService,
            AdminAuthService adminAuthService,
            ReconciliationService reconciliationService
    ) {
        this.paymentService = paymentService;
        this.auditService = auditService;
        this.adminAuthService = adminAuthService;
        this.reconciliationService = reconciliationService;
    }

    /** 관리자 결제 목록 조회 API */
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResultResponse>> payments(
            @RequestHeader("X-Admin-Token") String adminToken,
            @RequestParam(value = "status", required = false) PaymentStatus status
    ) {
        adminAuthService.validateToken(adminToken);
        List<PaymentResultResponse> response = paymentService.adminPayments(status)
                .stream()
                .map(PaymentResultResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /** 관리자 주문 목록 조회 API(기간 필터 지원) */
    @GetMapping("/orders")
    public ResponseEntity<List<AdminOrderResponse>> orders(
            @RequestHeader("X-Admin-Token") String adminToken,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        adminAuthService.validateToken(adminToken);
        List<AdminOrderResponse> response = paymentService.adminOrders(from, to)
                .stream()
                .map(AdminOrderResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /** 관리자 감사 로그 조회 API */
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponse>> logs(@RequestHeader("X-Admin-Token") String adminToken) {
        adminAuthService.validateToken(adminToken);
        List<AuditLogResponse> response = auditService.latest()
                .stream()
                .map(AuditLogResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /** 관리자 수동 보정 실행 API */
    @PostMapping("/reconcile-now")
    public ResponseEntity<Map<String, Integer>> reconcileNow(@RequestHeader("X-Admin-Token") String adminToken) {
        adminAuthService.validateToken(adminToken);
        int failedReady = reconciliationService.runCompensateStuckReadyPayments();
        int repairedOrders = reconciliationService.runRepairOrderPaymentMismatch();
        return ResponseEntity.ok(Map.of(
                "failedReadyPayments", failedReady,
                "repairedOrders", repairedOrders
        ));
    }
}
