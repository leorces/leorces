package com.leorces.persistence.postgres.repository.query.job;

public class FIND_ALL_WITH_PAGINATION {

    public static final String FIND_ALL_WITH_PAGINATION_QUERY = """
            SELECT *
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
            ORDER BY job_created_at DESC
            OFFSET :offset LIMIT :limit;
            """;

    private FIND_ALL_WITH_PAGINATION() {
    }

}
