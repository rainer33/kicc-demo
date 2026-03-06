package com.example.kiccdemo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis 기반 중복 요청 제어 서비스입니다.
 *
 * 최소 기능:
 * - lock 키(NX, TTL)로 동일 Idempotency-Key 동시 요청 선점
 * - 응답 결과(orderId) 캐시로 동일 요청 재호출 시 즉시 재사용
 */
@Service
public class RedisDedupService {

    private static final Logger log = LoggerFactory.getLogger(RedisDedupService.class);
    private static final String LOCK_PREFIX = "idem:ready:lock:";
    private static final String RESULT_PREFIX = "idem:ready:result:";

    private final StringRedisTemplate redisTemplate;

    public RedisDedupService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 이미 처리된 결과(orderId)가 있으면 반환합니다. */
    public Optional<String> getResultOrderId(String idempotencyKey) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(RESULT_PREFIX + idempotencyKey));
        } catch (DataAccessException e) {
            log.warn("Redis result lookup failed. fallback to local flow. key={}", idempotencyKey);
            return Optional.empty();
        }
    }

    /** 동일 키 선점을 시도합니다. 이미 선점 중이면 false입니다. */
    public boolean tryAcquireLock(String idempotencyKey, Duration ttl) {
        try {
            Boolean ok = redisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX + idempotencyKey, "1", ttl);
            return Boolean.TRUE.equals(ok);
        } catch (DataAccessException e) {
            log.warn("Redis lock acquire failed. allow request for availability. key={}", idempotencyKey);
            return true;
        }
    }

    /** 처리 완료 결과를 캐시합니다. */
    public void saveResult(String idempotencyKey, String orderId, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(RESULT_PREFIX + idempotencyKey, orderId, ttl);
        } catch (DataAccessException e) {
            log.warn("Redis result save failed. key={}, orderId={}", idempotencyKey, orderId);
        }
    }

    /** 선점 lock을 해제합니다. */
    public void releaseLock(String idempotencyKey) {
        try {
            redisTemplate.delete(LOCK_PREFIX + idempotencyKey);
        } catch (DataAccessException e) {
            log.warn("Redis lock release failed. key={}", idempotencyKey);
        }
    }
}
