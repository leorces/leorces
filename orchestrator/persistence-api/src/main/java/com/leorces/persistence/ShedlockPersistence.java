package com.leorces.persistence;


import java.time.Instant;


/**
 * Persistence layer for managing distributed locks using ShedLock mechanism.
 * Provides operations for acquiring and releasing locks to ensure exclusive execution across multiple instances.
 */
public interface ShedlockPersistence {

    /**
     * Attempts to acquire a distributed lock.
     *
     * @param name      the name of the lock to acquire
     * @param lockUntil the timestamp until which the lock should be held
     * @param lockedBy  the identifier of the lock holder
     * @return true if the lock was successfully acquired, false otherwise
     */
    boolean tryAcquireLock(String name, Instant lockUntil, String lockedBy);

    /**
     * Releases a previously acquired distributed lock.
     *
     * @param name the name of the lock to release
     */
    void releaseLock(String name);

}
