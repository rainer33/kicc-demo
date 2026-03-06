package com.example.kiccdemo.controller;

import com.example.kiccdemo.dto.KioskPaymentRequest;
import com.example.kiccdemo.dto.KioskSessionCreateRequest;
import com.example.kiccdemo.dto.KioskSessionResponse;
import com.example.kiccdemo.dto.KioskTerminalResultRequest;
import com.example.kiccdemo.service.KioskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 키오스크 연동 데모 API입니다.
 */
@RestController
@RequestMapping("/api/kiosk")
public class KioskController {

    private final KioskService kioskService;

    public KioskController(KioskService kioskService) {
        this.kioskService = kioskService;
    }

    /** 세션 생성 API */
    @PostMapping("/sessions")
    public ResponseEntity<KioskSessionResponse> create(@Valid @RequestBody KioskSessionCreateRequest request) {
        return ResponseEntity.ok(kioskService.createSession(request));
    }

    /** 결제요청 API */
    @PostMapping("/sessions/{sessionId}/request-payment")
    public ResponseEntity<KioskSessionResponse> requestPayment(
            @PathVariable String sessionId,
            @Valid @RequestBody KioskPaymentRequest request
    ) {
        return ResponseEntity.ok(kioskService.requestPayment(sessionId, request));
    }

    /** 단말 승인 이벤트 API */
    @PostMapping("/sessions/{sessionId}/terminal-approve")
    public ResponseEntity<KioskSessionResponse> terminalApprove(
            @PathVariable String sessionId,
            @Valid @RequestBody KioskTerminalResultRequest request
    ) {
        return ResponseEntity.ok(kioskService.approveByTerminal(sessionId, request));
    }

    /** 단말 실패 이벤트 API */
    @PostMapping("/sessions/{sessionId}/terminal-fail")
    public ResponseEntity<KioskSessionResponse> terminalFail(
            @PathVariable String sessionId,
            @Valid @RequestBody KioskTerminalResultRequest request
    ) {
        return ResponseEntity.ok(kioskService.failByTerminal(sessionId, request));
    }

    /** 세션 단건 조회 API */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<KioskSessionResponse> get(@PathVariable String sessionId) {
        return ResponseEntity.ok(kioskService.get(sessionId));
    }

    /** 최신 세션 목록 API */
    @GetMapping("/sessions")
    public ResponseEntity<List<KioskSessionResponse>> latest() {
        return ResponseEntity.ok(kioskService.latest());
    }
}
