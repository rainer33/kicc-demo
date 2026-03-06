package com.example.kiccdemo.dto;

import com.example.kiccdemo.entity.TestHistory;
import com.example.kiccdemo.entity.TestHistoryCategory;
import com.example.kiccdemo.entity.TestHistoryStatus;

import java.time.LocalDateTime;

/**
 * 테스트 이력 상세 화면용 DTO입니다.
 */
public class TestHistoryDetailResponse {

    private Long id;
    private String runId;
    private String scenarioName;
    private TestHistoryCategory category;
    private TestHistoryStatus status;
    private String environment;
    private String apiEndpoint;
    private String httpMethod;
    private String idempotencyKey;
    private Integer concurrencyLevel;
    private String networkCondition;
    private String expectedResult;
    private String actualResult;
    private String requestPayload;
    private String responsePayload;
    private String notes;
    private String executedBy;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long durationMs;

    public static TestHistoryDetailResponse from(TestHistory history) {
        TestHistoryDetailResponse response = new TestHistoryDetailResponse();
        response.id = history.getId();
        response.runId = history.getRunId();
        response.scenarioName = history.getScenarioName();
        response.category = history.getCategory();
        response.status = history.getStatus();
        response.environment = history.getEnvironment();
        response.apiEndpoint = history.getApiEndpoint();
        response.httpMethod = history.getHttpMethod();
        response.idempotencyKey = history.getIdempotencyKey();
        response.concurrencyLevel = history.getConcurrencyLevel();
        response.networkCondition = history.getNetworkCondition();
        response.expectedResult = history.getExpectedResult();
        response.actualResult = history.getActualResult();
        response.requestPayload = history.getRequestPayload();
        response.responsePayload = history.getResponsePayload();
        response.notes = history.getNotes();
        response.executedBy = history.getExecutedBy();
        response.startedAt = history.getStartedAt();
        response.finishedAt = history.getFinishedAt();
        response.durationMs = history.getDurationMs();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getRunId() {
        return runId;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public TestHistoryCategory getCategory() {
        return category;
    }

    public TestHistoryStatus getStatus() {
        return status;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Integer getConcurrencyLevel() {
        return concurrencyLevel;
    }

    public String getNetworkCondition() {
        return networkCondition;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public String getActualResult() {
        return actualResult;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public String getNotes() {
        return notes;
    }

    public String getExecutedBy() {
        return executedBy;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }
}
