package com.leorces.persistence.postgres.repository.query.history;

public final class FIND_ALL_WITH_PAGINATION {

    public static final String FIND_ALL_WITH_PAGINATION_QUERY = """
            SELECT *
            FROM history
            ORDER BY process_created_at DESC
            OFFSET :offset LIMIT :limit;
            """;

    private FIND_ALL_WITH_PAGINATION() {
        // Index: idx_history_created_at
    }

}
