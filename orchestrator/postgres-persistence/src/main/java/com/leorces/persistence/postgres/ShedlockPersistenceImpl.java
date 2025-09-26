package com.leorces.persistence.postgres;

import com.leorces.persistence.ShedlockPersistence;
import com.leorces.persistence.postgres.repository.ShedlockRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@AllArgsConstructor
public class ShedlockPersistenceImpl implements ShedlockPersistence {

    private final ShedlockRepository shedlockRepository;

    @Override
    @Transactional
    public boolean tryAcquireLock(String name, Instant lockUntil, String lockedBy) {
        return shedlockRepository.tryAcquireLock(name, lockedBy, lockUntil);
    }

    @Override
    @Transactional
    public void releaseLock(String name) {
        shedlockRepository.releaseLock(name);
    }

}
