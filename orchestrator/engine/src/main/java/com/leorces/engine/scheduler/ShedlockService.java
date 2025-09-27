package com.leorces.engine.scheduler;

import com.leorces.persistence.ShedlockPersistence;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import static com.leorces.common.utils.HostUtils.HOSTNAME;

@Slf4j
@Service
@AllArgsConstructor
public class ShedlockService {

    private final ShedlockPersistence shedlockPersistence;

    public <T> void executeWithLock(String lockName, Duration duration, Supplier<T> task) {
        var lockUntil = Instant.now().plus(duration);
        var acquired = shedlockPersistence.tryAcquireLock(lockName, lockUntil, HOSTNAME);

        if (!acquired) {
            log.info("Lock: {} is already held, skipping execution on node: {}", lockName, HOSTNAME);
            return;
        }

        try {
            log.info("Lock: {} acquired by node: {}", lockName, HOSTNAME);
            task.get();
        } catch (Exception e) {
            log.error("Error during locked task execution: {}", lockName, e);
            throw e;
        } finally {
            shedlockPersistence.releaseLock(lockName);
            log.info("Lock: {} released by node: {}", lockName, HOSTNAME);
        }
    }

}
