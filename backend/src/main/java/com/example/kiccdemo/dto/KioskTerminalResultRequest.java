package com.example.kiccdemo.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 키오스크 단말 승인/실패 이벤트 DTO입니다.
 */
public class KioskTerminalResultRequest {

    @NotBlank
    private String terminalTransactionId;

    private String message;

    public String getTerminalTransactionId() {
        return terminalTransactionId;
    }

    public void setTerminalTransactionId(String terminalTransactionId) {
        this.terminalTransactionId = terminalTransactionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
