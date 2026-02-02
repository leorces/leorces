package com.leorces.persistence.postgres.repository.query.process;

public class RUN {

    public static final String RUN_QUERY = """
            INSERT INTO process (process_id,
                                 root_process_id,
                                 process_parent_id,
                                 process_definition_id,
                                 process_definition_key,
                                 process_business_key,
                                 process_state,
                                 process_suspended,
                                 process_created_at,
                                 process_updated_at,
                                 process_started_at)
            SELECT :processId,
                   :rootProcessId,
                   :parentProcessId,
                   :definitionId,
                   :definitionKey,
                   :businessKey,
                   'ACTIVE',
                   CASE
                       WHEN :suspended = TRUE THEN TRUE
                       ELSE COALESCE(d.definition_suspended, FALSE)
                       END,
                   NOW(),
                   NOW(),
                   NOW()
            FROM (SELECT 1) v
                     LEFT JOIN definition d
                               ON d.definition_id = :definitionId
            RETURNING *;
            """;

    private RUN() {
        // Index: pk_definition
    }

}
