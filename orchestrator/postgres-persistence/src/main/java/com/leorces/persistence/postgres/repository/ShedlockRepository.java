package com.leorces.persistence.postgres.repository;

import com.leorces.persistence.postgres.entity.ShedlockEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.leorces.persistence.postgres.repository.query.ShedlockQueries.RELEASE_LOCK_QUERY;
import static com.leorces.persistence.postgres.repository.query.ShedlockQueries.TRY_ACQUIRE_LOCK_QUERY;

@Repository
public interface ShedlockRepository extends CrudRepository<ShedlockEntity, String> {

    @Modifying
    @Query(TRY_ACQUIRE_LOCK_QUERY)
    int tryAcquireLockQuery(@Param("name") String name,
                            @Param("lockedBy") String lockedBy,
                            @Param("lockUntil") LocalDateTime lockUntil,
                            @Param("now") LocalDateTime now);

    @Modifying
    @Query(RELEASE_LOCK_QUERY)
    void releaseLockQuery(@Param("name") String name, @Param("now") LocalDateTime now);

    default boolean tryAcquireLock(String name, String lockedBy, Instant lockUntil) {
        var now = LocalDateTime.now();
        var lockUntilDateTime = LocalDateTime.ofInstant(lockUntil, ZoneOffset.UTC);
        int updated = tryAcquireLockQuery(name, lockedBy, lockUntilDateTime, now);
        return updated > 0;
    }

    default void releaseLock(String name) {
        releaseLockQuery(name, LocalDateTime.now());
    }

}
