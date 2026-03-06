package com.example.kiccdemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_histories")
/**
 * 테스트 실행 이력 엔티티입니다.
 *
 * 컬럼 설계 의도:
 * - 현재 수행한 테스트뿐 아니라 앞으로 진행할 동시성/네트워크 장애/보안 시나리오까지
 *   동일한 테이블에서 축적할 수 있도록 범용 메타데이터를 포함합니다.
 * - 시나리오, 기대결과, 실제결과, 환경정보를 함께 저장해 회귀 분석 근거를 남깁니다.
 */
public class TestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String runId;

    @Column(nullable = false, length = 160)
    private String scenarioName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TestHistoryCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TestHistoryStatus status;

    @Column(nullable = false, length = 80)
    private String environment;

    @Column(length = 160)
    private String apiEndpoint;

    @Column(length = 12)
    private String httpMethod;

    @Column(length = 120)
    private String idempotencyKey;

    @Column
    private Integer concurrencyLevel;

    @Column(length = 80)
    private String networkCondition;

    @Column(nullable = false, length = 1000)
    private String expectedResult;

    @Column(nullable = false, length = 2000)
    private String actualResult;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String requestPayload;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String responsePayload;

    @Column(length = 1200)
    private String notes;

    @Column(nullable = false, length = 80)
    private String executedBy;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime finishedAt;

    @Column
    private Long durationMs;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public TestHistoryCategory getCategory() {
        return category;
    }

    public void setCategory(TestHistoryCategory category) {
        this.category = category;
    }

    public TestHistoryStatus getStatus() {
        return status;
    }

    public void setStatus(TestHistoryStatus status) {
        this.status = status;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Integer getConcurrencyLevel() {
        return concurrencyLevel;
    }

    public void setConcurrencyLevel(Integer concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
    }

    public String getNetworkCondition() {
        return networkCondition;
    }

    public void setNetworkCondition(String networkCondition) {
        this.networkCondition = networkCondition;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public String getActualResult() {
        return actualResult;
    }

    public void setActualResult(String actualResult) {
        this.actualResult = actualResult;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(String executedBy) {
        this.executedBy = executedBy;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
