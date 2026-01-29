package com.leorces.persistence.postgres.repository.query.shedlock;

public final class RELEASE_LOCK {

    public static final String RELEASE_LOCK_QUERY = """
            UPDATE shedlock
            SET lock_until = :now
            WHERE name = :name;
            """;

    private RELEASE_LOCK() {
    }

}
