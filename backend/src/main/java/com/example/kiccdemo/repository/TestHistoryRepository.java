package com.example.kiccdemo.repository;

import com.example.kiccdemo.entity.TestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 테스트 이력 조회/저장을 담당하는 리포지토리입니다.
 *
 * JpaSpecificationExecutor를 함께 사용해 필터(카테고리/상태/키워드) 조합 조회를 지원합니다.
 */
public interface TestHistoryRepository extends JpaRepository<TestHistory, Long>, JpaSpecificationExecutor<TestHistory> {
}
