package com.leorces.persistence.postgres.repository.query.activity;

public class IS_ANY_FAILED {

    public static final String IS_ANY_FAILED_QUERY = """
            SELECT EXISTS (SELECT 1
                           FROM activity
                           WHERE process_id = :processId
                             AND activity_state = 'FAILED');
            """;

    private IS_ANY_FAILED() {
        // Index: idx_activity_process_state
    }

}
