package com.leorces.persistence.postgres;

import com.leorces.persistence.ShedlockPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class ShedlockPersistenceIT extends RepositoryIT {

    @Autowired
    private ShedlockPersistence subject;

    @Test
    @DisplayName("Should successfully acquire lock when no existing lock exists")
    void tryAcquireLockSuccess() {
        // Given
        var lockName = "test-lock";
        var lockUntil = Instant.now().plus(24, ChronoUnit.HOURS);
        var lockedBy = "test-instance";

        // When
        var result = subject.tryAcquireLock(lockName, lockUntil, lockedBy);

        // Then
        assertTrue(result);

        // Verify lock was created in database
        var lockEntity = shedlockRepository.findById(lockName);
        assertTrue(lockEntity.isPresent());
        assertEquals(lockName, lockEntity.get().getName());
        assertEquals(lockedBy, lockEntity.get().getLockedBy());
        assertNotNull(lockEntity.get().getLockedAt());
        assertNotNull(lockEntity.get().getLockUntil());
    }

    @Test
    @DisplayName("Should fail to acquire lock when active lock exists")
    void tryAcquireLockFailureActiveLockExists() {
        // Given
        var lockName = "test-lock";
        // Use a much longer lock duration to ensure it doesn't expire during test
        var lockUntil = Instant.now().plus(24, ChronoUnit.HOURS);
        var lockedBy1 = "instance-1";
        var lockedBy2 = "instance-2";

        // First instance acquires the lock
        var firstResult = subject.tryAcquireLock(lockName, lockUntil, lockedBy1);
        assertTrue(firstResult);

        // When
        // Second instance tries to acquire the same lock (should fail because lock is still active)
        var secondResult = subject.tryAcquireLock(lockName, lockUntil, lockedBy2);

        // Then
        assertFalse(secondResult);

        // Verify original lock still exists and unchanged
        var lockEntity = shedlockRepository.findById(lockName);
        assertTrue(lockEntity.isPresent());
        assertEquals(lockedBy1, lockEntity.get().getLockedBy());
    }

    @Test
    @DisplayName("Should successfully acquire lock when previous lock has expired")
    void tryAcquireLockSuccessAfterExpiration() {
        // Given
        var lockName = "test-lock";
        var expiredLockUntil = Instant.now().minus(1, ChronoUnit.HOURS);
        var newLockUntil = Instant.now().plus(24, ChronoUnit.HOURS);
        var lockedBy1 = "instance-1";
        var lockedBy2 = "instance-2";

        // First instance acquires lock with past expiration time
        var firstResult = subject.tryAcquireLock(lockName, expiredLockUntil, lockedBy1);
        assertTrue(firstResult);

        // When
        // Second instance tries to acquire the expired lock
        var secondResult = subject.tryAcquireLock(lockName, newLockUntil, lockedBy2);

        // Then
        assertTrue(secondResult);

        // Verify lock was updated with new instance
        var lockEntity = shedlockRepository.findById(lockName);
        assertTrue(lockEntity.isPresent());
        assertEquals(lockedBy2, lockEntity.get().getLockedBy());
    }

    @Test
    @DisplayName("Should release existing lock successfully")
    void releaseLockSuccess() {
        // Given
        var lockName = "test-lock";
        var lockUntil = Instant.now().plus(24, ChronoUnit.HOURS);
        var lockedBy = "test-instance";

        // Acquire lock first
        var acquireResult = subject.tryAcquireLock(lockName, lockUntil, lockedBy);
        assertTrue(acquireResult);

        // Verify lock exists
        var lockExists = shedlockRepository.existsById(lockName);
        assertTrue(lockExists);

        // When
        subject.releaseLock(lockName);

        // Then
        // Verify lock was removed or updated (depending on implementation)
        // Since we're using a release query that likely updates the lock_until to current time,
        // we should verify the lock is effectively released
        var newAcquireResult = subject.tryAcquireLock(lockName, Instant.now().plus(24, ChronoUnit.HOURS), "another-instance");
        assertTrue(newAcquireResult);
    }

    @Test
    @DisplayName("Should not fail when releasing non-existent lock")
    void releaseLockNonExistent() {
        // Given
        var lockName = "non-existent-lock";

        // When & Then
        // Should not throw exception
        assertDoesNotThrow(() -> subject.releaseLock(lockName));
    }

    @Test
    @DisplayName("Should handle multiple concurrent lock attempts correctly")
    void multipleConcurrentLockAttempts() {
        // Given
        var lockName = "concurrent-lock";
        var lockUntil = Instant.now().plus(24, ChronoUnit.HOURS);
        var lockedBy1 = "instance-1";
        var lockedBy2 = "instance-2";
        var lockedBy3 = "instance-3";

        // When
        var result1 = subject.tryAcquireLock(lockName, lockUntil, lockedBy1);
        var result2 = subject.tryAcquireLock(lockName, lockUntil, lockedBy2);
        var result3 = subject.tryAcquireLock(lockName, lockUntil, lockedBy3);

        // Then
        // Only one should succeed
        var successCount = (result1 ? 1 : 0) + (result2 ? 1 : 0) + (result3 ? 1 : 0);
        assertEquals(1, successCount);

        // The successful one should be the first
        assertTrue(result1);
        assertFalse(result2);
        assertFalse(result3);
    }

    @Test
    @DisplayName("Should handle lock acquisition and release cycle")
    void lockAcquisitionReleaseCycle() {
        // Given
        var lockName = "cycle-lock";
        var lockUntil = Instant.now().plus(24, ChronoUnit.HOURS);
        var lockedBy1 = "instance-1";
        var lockedBy2 = "instance-2";

        // When & Then
        // First acquisition
        var firstAcquire = subject.tryAcquireLock(lockName, lockUntil, lockedBy1);
        assertTrue(firstAcquire);

        // Second instance fails to acquire
        var secondAcquire = subject.tryAcquireLock(lockName, lockUntil, lockedBy2);
        assertFalse(secondAcquire);

        // Release lock
        subject.releaseLock(lockName);

        // Second instance can now acquire
        var thirdAcquire = subject.tryAcquireLock(lockName, lockUntil, lockedBy2);
        assertTrue(thirdAcquire);
    }

}