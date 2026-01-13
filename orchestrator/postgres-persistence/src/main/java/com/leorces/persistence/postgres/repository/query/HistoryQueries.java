package com.leorces.persistence.postgres.repository.query;

public final class HistoryQueries {

    public static final String FIND_ALL_WITH_PAGINATION = """
            SELECT *
            FROM history
            ORDER BY process_created_at DESC
            OFFSET :offset
            LIMIT :limit
            """;

    private HistoryQueries() {
        // Utility class
    }

}