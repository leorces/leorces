package com.leorces.persistence.postgres.repository.query.activity;

public class DELETE_ALL_ACTIVE_BY_DEFINITION_IDS {

    public static final String DELETE_ALL_ACTIVE_BY_DEFINITION_IDS_QUERY = """
            UPDATE activity
            SET activity_state      = 'DELETED',
                activity_updated_at = NOW()
            WHERE process_id = :processId
              AND activity_definition_id = ANY (:definitionIds)
              AND activity_state IN ('ACTIVE', 'SCHEDULED');
            """;

    private DELETE_ALL_ACTIVE_BY_DEFINITION_IDS() {
        // Index: idx_activity_process_def_state
    }

}
