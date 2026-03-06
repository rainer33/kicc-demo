package com.example.kiccdemo.controller;

import com.example.kiccdemo.dto.TestHistoryDetailResponse;
import com.example.kiccdemo.dto.TestHistoryCreateRequest;
import com.example.kiccdemo.dto.TestHistoryPageResponse;
import com.example.kiccdemo.entity.TestHistoryCategory;
import com.example.kiccdemo.entity.TestHistoryStatus;
import com.example.kiccdemo.service.AdminAuthService;
import com.example.kiccdemo.service.TestHistoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 테스트 이력 조회 API입니다.
 *
 * 화면 요구사항(목록/상세)에 맞춰 조회 기능을 분리 제공합니다.
 */
@RestController
@RequestMapping("/api/admin/test-histories")
public class AdminTestHistoryController {

    private final AdminAuthService adminAuthService;
    private final TestHistoryService testHistoryService;

    public AdminTestHistoryController(AdminAuthService adminAuthService, TestHistoryService testHistoryService) {
        this.adminAuthService = adminAuthService;
        this.testHistoryService = testHistoryService;
    }

    /**
     * 테스트 이력 목록 조회 API입니다.
     */
    @GetMapping
    public ResponseEntity<TestHistoryPageResponse> list(
            @RequestHeader("X-Admin-Token") String adminToken,
            @RequestParam(value = "category", required = false) TestHistoryCategory category,
            @RequestParam(value = "status", required = false) TestHistoryStatus status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        adminAuthService.validateToken(adminToken);
        return ResponseEntity.ok(testHistoryService.list(category, status, keyword, page, size));
    }

    /**
     * 테스트 이력 상세 조회 API입니다.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TestHistoryDetailResponse> detail(
            @RequestHeader("X-Admin-Token") String adminToken,
            @PathVariable("id") Long id
    ) {
        adminAuthService.validateToken(adminToken);
        return ResponseEntity.ok(testHistoryService.detail(id));
    }

    /**
     * 테스트 이력 등록 API입니다.
     */
    @PostMapping
    public ResponseEntity<TestHistoryDetailResponse> create(
            @RequestHeader("X-Admin-Token") String adminToken,
            @Valid @RequestBody TestHistoryCreateRequest request
    ) {
        adminAuthService.validateToken(adminToken);
        return ResponseEntity.ok(testHistoryService.create(request));
    }
}
