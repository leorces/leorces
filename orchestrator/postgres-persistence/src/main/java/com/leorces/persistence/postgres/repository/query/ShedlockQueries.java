package com.leorces.persistence.postgres.repository.query;

public final class ShedlockQueries {

    public static final String TRY_ACQUIRE_LOCK_QUERY = """
            INSERT INTO shedlock (name, lock_until, locked_at, locked_by)
            VALUES (:name, :lockUntil, :now, :lockedBy)
            ON CONFLICT (name) DO UPDATE SET
                                  lock_until = EXCLUDED.lock_until,
                                  locked_at  = EXCLUDED.locked_at,
                                  locked_by  = EXCLUDED.locked_by
            WHERE shedlock.lock_until <= :now;
            """;

    public static final String RELEASE_LOCK_QUERY = """
            UPDATE shedlock
            SET lock_until = :now
            WHERE name = :name
            """;

    private ShedlockQueries() {
        // Utility class
    }

}