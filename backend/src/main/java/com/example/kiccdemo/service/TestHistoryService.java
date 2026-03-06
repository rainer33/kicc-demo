package com.example.kiccdemo.service;

import com.example.kiccdemo.dto.TestHistoryDetailResponse;
import com.example.kiccdemo.dto.TestHistoryCreateRequest;
import com.example.kiccdemo.dto.TestHistoryListItemResponse;
import com.example.kiccdemo.dto.TestHistoryPageResponse;
import com.example.kiccdemo.entity.TestHistory;
import com.example.kiccdemo.entity.TestHistoryCategory;
import com.example.kiccdemo.entity.TestHistoryStatus;
import com.example.kiccdemo.repository.TestHistoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 테스트 이력 관리 서비스입니다.
 *
 * 제공 기능:
 * - 목록/상세 조회
 * - 카테고리/상태/키워드 필터
 * - 최초 실행 시 샘플 이력 자동 적재(지금까지 수행한 테스트를 기록했다고 가정)
 */
@Service
public class TestHistoryService {

    private final TestHistoryRepository testHistoryRepository;

    public TestHistoryService(TestHistoryRepository testHistoryRepository) {
        this.testHistoryRepository = testHistoryRepository;
    }

    /**
     * 현재 저장된 데이터가 없을 때 초기 샘플 이력을 적재합니다.
     *
     * 운영 데이터가 이미 있으면 중복 삽입하지 않습니다.
     */
    @PostConstruct
    @Transactional
    public void seedInitialHistoriesIfEmpty() {
        if (testHistoryRepository.count() > 0) {
            return;
        }

        List<TestHistory> seeds = List.of(
                seed(
                        "RUN-20260307-001", "결제 ready 기본 흐름", TestHistoryCategory.PAYMENT_FLOW, TestHistoryStatus.PASS,
                        "local-dev", "/api/payments/ready", "POST", null, 1, "normal",
                        "결제 요청 시 orderId가 생성되고 READY 상태가 된다.",
                        "orderId 생성 및 formFields 반환 확인.",
                        "{\"orderName\":\"테스트 상품\",\"buyerName\":\"홍길동\",\"amount\":1000}",
                        "{\"orderId\":\"ORDER-...\",\"mockMode\":true}",
                        "프론트 결제 요청 화면 수동 검증.", "tester", LocalDateTime.now().minusHours(8), 480L
                ),
                seed(
                        "RUN-20260307-002", "Idempotency-Key 재요청 동일 응답", TestHistoryCategory.IDEMPOTENCY, TestHistoryStatus.PASS,
                        "local-dev", "/api/payments/ready", "POST", "idem-001", 1, "normal",
                        "동일 키로 두 번 호출하면 동일 orderId를 반환한다.",
                        "2회 호출에서 같은 orderId 반환 확인.",
                        "header: Idempotency-Key=idem-001", "same orderId", "DB 멱등 레코드 재사용 확인.",
                        "tester", LocalDateTime.now().minusHours(7), 520L
                ),
                seed(
                        "RUN-20260307-003", "Redis NX 락 기반 동시성 선점", TestHistoryCategory.CONCURRENCY, TestHistoryStatus.PASS,
                        "local-dev", "/api/payments/ready", "POST", "redis-idem-test-001", 20, "normal",
                        "동일 멱등키 동시 요청에서 단일 처리만 성공하고 나머지는 중복 처리된다.",
                        "선점 락 동작 및 결과 캐시 저장 확인.",
                        "parallel requests with same key", "one orderId reused", "락 TTL 30초 설정.",
                        "tester", LocalDateTime.now().minusHours(6), 950L
                ),
                seed(
                        "RUN-20260307-004", "Redis 인증 계정 연동", TestHistoryCategory.INFRA, TestHistoryStatus.PASS,
                        "local-dev", "redis://127.0.0.1:6379", "AUTH", null, 1, "normal",
                        "redis 계정/비밀번호로 애플리케이션이 정상 연결된다.",
                        "NOAUTH 오류 없이 API 요청 및 키 저장 확인.",
                        "user=redis", "PONG + key write", "ACL 파일 기반 인증 검증.",
                        "tester", LocalDateTime.now().minusHours(5), 430L
                ),
                seed(
                        "RUN-20260307-005", "콜백 서명 검증 차단", TestHistoryCategory.CALLBACK_SECURITY, TestHistoryStatus.PASS,
                        "local-dev", "/api/payments/kicc/callback", "POST", null, 1, "normal",
                        "서명 누락/불일치 콜백은 거부되어야 한다.",
                        "서명 검증 실패 시 FORBIDDEN 응답 확인.",
                        "invalid signature", "403 forbidden", "위변조 방어 기본값 true.",
                        "tester", LocalDateTime.now().minusHours(4), 610L
                ),
                seed(
                        "RUN-20260307-006", "mock 승인 후 전체취소", TestHistoryCategory.CANCEL, TestHistoryStatus.PASS,
                        "local-dev", "/api/payments/{orderId}/mock-cancel", "POST", null, 1, "normal",
                        "승인된 결제만 전체취소 가능해야 한다.",
                        "APPROVED -> CANCELED 전이 확인.",
                        "orderId flow", "status changed to CANCELED", "취소 후 주문 상태 동기화 확인.",
                        "tester", LocalDateTime.now().minusHours(3), 700L
                ),
                seed(
                        "RUN-20260307-007", "부분환불 누적 상태 전이", TestHistoryCategory.REFUND, TestHistoryStatus.PASS,
                        "local-dev", "/api/payments/{orderId}/mock-refund", "POST", null, 1, "normal",
                        "부분환불 시 PARTIALLY_REFUNDED, 전액 환불 시 REFUNDED로 전이된다.",
                        "누적 환불금액 계산 및 환불이력 저장 확인.",
                        "{amount:100}", "refundedAmount updated", "환불 가능 금액 초과 차단 포함.",
                        "tester", LocalDateTime.now().minusHours(2), 840L
                ),
                seed(
                        "RUN-20260307-008", "관리자 수동 보정 실행", TestHistoryCategory.RECONCILIATION, TestHistoryStatus.PASS,
                        "local-dev", "/api/admin/reconcile-now", "POST", null, 1, "normal",
                        "보정 실행 시 정합성 회복 건수가 반환된다.",
                        "실행 결과 카운트 응답 및 감사로그 기록 확인.",
                        "manual reconcile", "{failedReadyPayments:0,repairedOrders:0}",
                        "정기 스케줄 외 수동 실행 검증.", "tester", LocalDateTime.now().minusHours(1), 390L
                )
        );

        testHistoryRepository.saveAll(seeds);
    }

    /**
     * 테스트 이력 목록을 페이징 조회합니다.
     */
    @Transactional(readOnly = true)
    public TestHistoryPageResponse list(
            TestHistoryCategory category,
            TestHistoryStatus status,
            String keyword,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "startedAt"));

        Specification<TestHistory> specification = Specification.where(null);
        if (category != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }
        if (status != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (StringUtils.hasText(keyword)) {
            String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("scenarioName")), likeKeyword),
                    cb.like(cb.lower(root.get("runId")), likeKeyword),
                    cb.like(cb.lower(root.get("apiEndpoint")), likeKeyword)
            ));
        }

        Page<TestHistory> result = testHistoryRepository.findAll(specification, pageable);
        List<TestHistoryListItemResponse> content = result.getContent().stream()
                .map(TestHistoryListItemResponse::from)
                .toList();

        return new TestHistoryPageResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    /**
     * 테스트 이력 상세를 조회합니다.
     */
    @Transactional(readOnly = true)
    public TestHistoryDetailResponse detail(Long id) {
        TestHistory history = testHistoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test history not found: id=" + id));
        return TestHistoryDetailResponse.from(history);
    }

    /**
     * 테스트 이력을 수동 등록합니다.
     */
    @Transactional
    public TestHistoryDetailResponse create(TestHistoryCreateRequest request) {
        TestHistory history = new TestHistory();
        history.setRunId(request.getRunId());
        history.setScenarioName(request.getScenarioName());
        history.setCategory(request.getCategory());
        history.setStatus(request.getStatus());
        history.setEnvironment(request.getEnvironment());
        history.setApiEndpoint(request.getApiEndpoint());
        history.setHttpMethod(request.getHttpMethod());
        history.setIdempotencyKey(request.getIdempotencyKey());
        history.setConcurrencyLevel(request.getConcurrencyLevel());
        history.setNetworkCondition(request.getNetworkCondition());
        history.setExpectedResult(request.getExpectedResult());
        history.setActualResult(request.getActualResult());
        history.setRequestPayload(request.getRequestPayload());
        history.setResponsePayload(request.getResponsePayload());
        history.setNotes(request.getNotes());
        history.setExecutedBy(request.getExecutedBy());

        LocalDateTime startedAt = LocalDateTime.now();
        history.setStartedAt(startedAt);
        Long durationMs = request.getDurationMs();
        if (durationMs != null && durationMs >= 0) {
            history.setDurationMs(durationMs);
            history.setFinishedAt(startedAt.plusNanos(durationMs * 1_000_000));
        } else {
            history.setDurationMs(null);
            history.setFinishedAt(null);
        }

        TestHistory saved = testHistoryRepository.save(history);
        return TestHistoryDetailResponse.from(saved);
    }

    private TestHistory seed(
            String runId,
            String scenarioName,
            TestHistoryCategory category,
            TestHistoryStatus status,
            String environment,
            String apiEndpoint,
            String httpMethod,
            String idempotencyKey,
            Integer concurrencyLevel,
            String networkCondition,
            String expectedResult,
            String actualResult,
            String requestPayload,
            String responsePayload,
            String notes,
            String executedBy,
            LocalDateTime startedAt,
            Long durationMs
    ) {
        TestHistory history = new TestHistory();
        history.setRunId(runId);
        history.setScenarioName(scenarioName);
        history.setCategory(category);
        history.setStatus(status);
        history.setEnvironment(environment);
        history.setApiEndpoint(apiEndpoint);
        history.setHttpMethod(httpMethod);
        history.setIdempotencyKey(idempotencyKey);
        history.setConcurrencyLevel(concurrencyLevel);
        history.setNetworkCondition(networkCondition);
        history.setExpectedResult(expectedResult);
        history.setActualResult(actualResult);
        history.setRequestPayload(requestPayload);
        history.setResponsePayload(responsePayload);
        history.setNotes(notes);
        history.setExecutedBy(executedBy);
        history.setStartedAt(startedAt);
        history.setFinishedAt(startedAt.plusNanos(durationMs * 1_000_000));
        history.setDurationMs(durationMs);
        return history;
    }
}
