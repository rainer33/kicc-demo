package com.example.kiccdemo.service;

import com.example.kiccdemo.entity.IdempotencyRecord;
import com.example.kiccdemo.repository.IdempotencyRecordRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 멱등키 예약 전용 서비스입니다. 별도 트랜잭션으로 동시성 충돌을 흡수합니다.
 *
 * 핵심 아이디어:
 * - 멱등키 reserve 작업을 REQUIRES_NEW 트랜잭션으로 분리해
 *   본 결제 트랜잭션과 독립적으로 충돌 처리 결과를 확정합니다.
 * - unique 제약 충돌이 나더라도 기존 레코드를 재조회해 정상 흐름으로 복구합니다.
 */
@Service
public class IdempotencyService {

    private final IdempotencyRecordRepository idempotencyRecordRepository;

    public IdempotencyService(IdempotencyRecordRepository idempotencyRecordRepository) {
        this.idempotencyRecordRepository = idempotencyRecordRepository;
    }

    /**
     * 멱등키를 예약하거나 기존 예약 결과를 반환합니다.
     *
     * @param idemKey 멱등키 원문(이미 상위 레이어에서 trim 처리됨)
     * @param requestHash 요청 바디 해시
     * @return Reservation(record, existing)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Reservation reserve(String idemKey, String requestHash) {
        IdempotencyRecord existing = idempotencyRecordRepository.findByIdemKey(idemKey).orElse(null);
        if (existing != null) {
            validateRequestHash(existing, requestHash);
            return new Reservation(existing, true);
        }

        try {
            IdempotencyRecord created = new IdempotencyRecord();
            created.setIdemKey(idemKey);
            created.setRequestHash(requestHash);
            IdempotencyRecord saved = idempotencyRecordRepository.saveAndFlush(created);
            return new Reservation(saved, false);
        } catch (DataIntegrityViolationException e) {
            IdempotencyRecord conflict = idempotencyRecordRepository.findByIdemKey(idemKey)
                    .orElseThrow(() -> new IllegalStateException("Failed to resolve idempotency key"));
            validateRequestHash(conflict, requestHash);
            return new Reservation(conflict, true);
        }
    }

    /** 같은 멱등키에 다른 요청 바디가 들어오면 즉시 차단합니다. */
    private void validateRequestHash(IdempotencyRecord record, String requestHash) {
        if (!record.getRequestHash().equals(requestHash)) {
            throw new IllegalArgumentException("Idempotency key already used with different request");
        }
    }

    /** 멱등키 예약 결과 전달용 불변 레코드입니다. */
    public record Reservation(IdempotencyRecord record, boolean existing) {
    }
}
