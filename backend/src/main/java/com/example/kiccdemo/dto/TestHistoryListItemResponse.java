package com.example.kiccdemo.dto;

import com.example.kiccdemo.entity.TestHistory;
import com.example.kiccdemo.entity.TestHistoryCategory;
import com.example.kiccdemo.entity.TestHistoryStatus;

import java.time.LocalDateTime;

/**
 * 테스트 이력 목록 화면용 요약 DTO입니다.
 */
public class TestHistoryListItemResponse {

    private Long id;
    private String runId;
    private String scenarioName;
    private TestHistoryCategory category;
    private TestHistoryStatus status;
    private String environment;
    private Integer concurrencyLevel;
    private String networkCondition;
    private String executedBy;
    private LocalDateTime startedAt;
    private Long durationMs;

    public static TestHistoryListItemResponse from(TestHistory history) {
        TestHistoryListItemResponse response = new TestHistoryListItemResponse();
        response.id = history.getId();
        response.runId = history.getRunId();
        response.scenarioName = history.getScenarioName();
        response.category = history.getCategory();
        response.status = history.getStatus();
        response.environment = history.getEnvironment();
        response.concurrencyLevel = history.getConcurrencyLevel();
        response.networkCondition = history.getNetworkCondition();
        response.executedBy = history.getExecutedBy();
        response.startedAt = history.getStartedAt();
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

    public Integer getConcurrencyLevel() {
        return concurrencyLevel;
    }

    public String getNetworkCondition() {
        return networkCondition;
    }

    public String getExecutedBy() {
        return executedBy;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }
}
