package com.leorces.persistence.postgres.repository.query.activity;

public class IS_ALL_COMPLETED_BY_PROCESS_ID {

    public static final String IS_ALL_COMPLETED_BY_PROCESS_ID_QUERY = """
            SELECT NOT EXISTS (SELECT 1
                               FROM activity
                               WHERE process_id = :processId
                                 AND activity_state NOT IN ('COMPLETED', 'TERMINATED', 'DELETED')
                                 AND activity_async = false);
            """;

    private IS_ALL_COMPLETED_BY_PROCESS_ID() {
    }

}
