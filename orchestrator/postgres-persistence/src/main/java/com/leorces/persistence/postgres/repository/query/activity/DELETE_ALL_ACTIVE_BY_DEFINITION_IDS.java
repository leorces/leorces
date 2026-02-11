package com.leorces.persistence.postgres.repository.query.activity;

public class DELETE_ALL_ACTIVE_BY_DEFINITION_IDS {

    public static final String DELETE_ALL_ACTIVE_BY_DEFINITION_IDS_QUERY = """
            DELETE FROM activity
            WHERE process_id = :processId
              AND activity_definition_id = ANY (:definitionIds)
              AND activity_state IN ('ACTIVE', 'SCHEDULED')
            RETURNING activity_id;
            """;

    private DELETE_ALL_ACTIVE_BY_DEFINITION_IDS() {
        // Index: idx_activity_process_def_state
    }

}
