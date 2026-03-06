package com.example.kiccdemo.service;

import com.example.kiccdemo.entity.IdempotencyRecord;
import com.example.kiccdemo.repository.IdempotencyRecordRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {

    private final IdempotencyRecordRepository idempotencyRecordRepository;

    public IdempotencyService(IdempotencyRecordRepository idempotencyRecordRepository) {
        this.idempotencyRecordRepository = idempotencyRecordRepository;
    }

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

    private void validateRequestHash(IdempotencyRecord record, String requestHash) {
        if (!record.getRequestHash().equals(requestHash)) {
            throw new IllegalArgumentException("Idempotency key already used with different request");
        }
    }

    public record Reservation(IdempotencyRecord record, boolean existing) {
    }
}
