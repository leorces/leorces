package com.leorces.persistence.postgres.repository.query.job;

public class COUNT_ALL_WITH_FILTERS {

    public static final String COUNT_ALL_WITH_FILTERS_QUERY = """
            SELECT COUNT(*)
            FROM job
            WHERE (
                :state IS NULL
                    OR :state = 'all'
                    OR job_state = :state
                )
              AND (
                :filter IS NULL
                    OR job_id ILIKE '%' || :filter || '%'
                    OR job_type ILIKE '%' || :filter || '%'
                    OR job_state ILIKE '%' || :filter || '%'
                )
            """;

    private COUNT_ALL_WITH_FILTERS() {
    }

}
