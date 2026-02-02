package com.leorces.persistence.postgres.repository.query.process;

public class FIND_ALL_WITH_PAGINATION {

    public static final String FIND_ALL_WITH_PAGINATION_QUERY = """
            SELECT process_id,
                   root_process_id,
                   process_parent_id,
                   process_definition_id,
                   process_definition_key,
                   process_business_key,
                   process_state,
                   process_suspended,
                   process_created_at,
                   process_updated_at,
                   process_started_at,
                   process_completed_at,
            
                   definition_id,
                   definition_key,
                   definition_name,
                   definition_version,
                   definition_suspended,
                   definition_data
            FROM process
                     LEFT JOIN definition ON process_definition_id = definition_id
            WHERE (
                :filter IS NULL OR :filter = '' OR
                process_id = :filter OR
                process_definition_key = :filter OR
                process_business_key = :filter
                )
              AND (
                :state IS NULL OR :state = '' OR :state = 'all' OR process_state = :state
                )
            ORDER BY process_created_at DESC
            OFFSET :offset LIMIT :limit;
            """;

    private FIND_ALL_WITH_PAGINATION() {
        // Index: idx_process_created_at, pk_process, idx_process_definition_key, idx_process_business_key
    }

}
