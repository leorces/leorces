package com.leorces.persistence.postgres.repository.query.process;

public class SUSPEND_BY_DEFINITION_KEY {

    public static final String SUSPEND_BY_DEFINITION_KEY_QUERY = """
            WITH RECURSIVE child_processes AS (SELECT process_id
                                               FROM process
                                               WHERE process_definition_key = :definitionKey
                                                 AND process_state IN ('ACTIVE', 'INCIDENT')
                                                 AND process_suspended = FALSE
            
                                               UNION ALL
            
                                               SELECT p.process_id
                                               FROM process p
                                                        JOIN child_processes cp ON p.process_parent_id = cp.process_id
                                               WHERE p.process_state IN ('ACTIVE', 'INCIDENT'))
            UPDATE process
            SET process_suspended  = TRUE,
                process_updated_at = NOW()
            WHERE process_id = ANY (SELECT process_id FROM child_processes);
            """;

    private SUSPEND_BY_DEFINITION_KEY() {
    }

}
