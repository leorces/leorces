package com.leorces.persistence.postgres.repository.query.process;

public class COUNT_ALL_WITH_FILTERS {

    public static final String COUNT_ALL_WITH_FILTERS_QUERY = """
            SELECT COUNT(*)
            FROM process p
                     LEFT JOIN definition d ON p.process_definition_id = d.definition_id
            WHERE (:filter IS NULL OR :filter = '' OR
                   p.process_id = :filter OR
                   p.process_definition_key = :filter OR
                   p.process_business_key = :filter OR
                   d.definition_name = :filter)
              AND (:state IS NULL OR :state = '' OR :state = 'all' OR p.process_state = :state)
            """;

    private COUNT_ALL_WITH_FILTERS() {
        // Index: pk_process, idx_process_definition_key, idx_process_business_key
    }

}
