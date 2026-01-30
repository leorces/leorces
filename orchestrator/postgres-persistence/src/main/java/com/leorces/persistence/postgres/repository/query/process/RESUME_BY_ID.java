package com.leorces.persistence.postgres.repository.query.process;

public class RESUME_BY_ID {

    public static final String RESUME_BY_ID_QUERY = """
            WITH RECURSIVE child_processes AS (SELECT process_id
                                               FROM process
                                               WHERE process_id = :processId
                                                 AND process_state IN ('ACTIVE', 'INCIDENT')
                                                 AND process_suspended = TRUE
            
                                               UNION ALL
            
                                               SELECT p.process_id
                                               FROM process p
                                                        JOIN child_processes cp ON p.process_parent_id = cp.process_id
                                               WHERE p.process_state IN ('ACTIVE', 'INCIDENT'))
            UPDATE process
            SET process_suspended  = FALSE,
                process_updated_at = NOW()
            WHERE process_id = ANY (SELECT process_id FROM child_processes);
            """;

    private RESUME_BY_ID() {
        // Index: pk_process, idx_process_parent_id
    }

}
