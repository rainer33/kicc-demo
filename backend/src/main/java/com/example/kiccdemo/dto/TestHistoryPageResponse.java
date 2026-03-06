package com.example.kiccdemo.dto;

import java.util.List;

/**
 * 테스트 이력 목록의 페이징 응답 DTO입니다.
 */
public class TestHistoryPageResponse {

    private List<TestHistoryListItemResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public TestHistoryPageResponse(
            List<TestHistoryListItemResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<TestHistoryListItemResponse> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
