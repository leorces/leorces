package com.leorces.persistence.postgres.repository.query.process;

public class RESUME_BY_DEFINITION_ID {

    public static final String RESUME_BY_DEFINITION_ID_QUERY = """
            WITH RECURSIVE child_processes AS (SELECT root_processes.process_id
                                               FROM (SELECT process_id
                                                     FROM process
                                                     WHERE process_definition_id = :definitionId
                                                       AND process_state IN ('ACTIVE', 'INCIDENT')
                                                       AND process_suspended = TRUE
                                                     ORDER BY process_id
                                                     LIMIT :limit) AS root_processes
            
                                               UNION ALL
            
                                               SELECT p.process_id
                                               FROM process p
                                                        JOIN child_processes cp
                                                             ON p.process_parent_id = cp.process_id
                                               WHERE p.process_state IN ('ACTIVE', 'INCIDENT'))
            
            UPDATE process pr
            SET process_suspended  = FALSE,
                process_updated_at = NOW()
            FROM child_processes cp
            WHERE pr.process_id = cp.process_id
            RETURNING pr.process_id;
            """;

    private RESUME_BY_DEFINITION_ID() {
    }

}
