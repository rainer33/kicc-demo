package com.example.kiccdemo.dto;

import com.example.kiccdemo.entity.TestHistoryCategory;
import com.example.kiccdemo.entity.TestHistoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 테스트 이력 등록 요청 DTO입니다.
 */
public class TestHistoryCreateRequest {

    @NotBlank
    @Size(max = 64)
    private String runId;

    @NotBlank
    @Size(max = 160)
    private String scenarioName;

    @NotNull
    private TestHistoryCategory category;

    @NotNull
    private TestHistoryStatus status;

    @NotBlank
    @Size(max = 80)
    private String environment;

    @Size(max = 160)
    private String apiEndpoint;

    @Size(max = 12)
    private String httpMethod;

    @Size(max = 120)
    private String idempotencyKey;

    private Integer concurrencyLevel;

    @Size(max = 80)
    private String networkCondition;

    @NotBlank
    @Size(max = 1000)
    private String expectedResult;

    @NotBlank
    @Size(max = 2000)
    private String actualResult;

    private String requestPayload;
    private String responsePayload;

    @Size(max = 1200)
    private String notes;

    @NotBlank
    @Size(max = 80)
    private String executedBy;

    private Long durationMs;

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

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
}
