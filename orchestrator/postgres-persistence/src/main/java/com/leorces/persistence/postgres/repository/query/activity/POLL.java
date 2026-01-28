package com.leorces.persistence.postgres.repository.query.activity;

public class POLL {

    public static final String POLL_QUERY = """
            WITH candidates AS (SELECT activity.activity_id
                                FROM activity
                                         JOIN process ON process.process_id = activity.process_id
                                WHERE activity.activity_topic = :topic
                                  AND activity.process_definition_key = :processDefinitionKey
                                  AND activity.activity_state = 'SCHEDULED'
                                  AND process.process_suspended = FALSE
                                ORDER BY activity.activity_created_at
                                LIMIT :limit FOR UPDATE OF activity SKIP LOCKED),
                 updated AS (
                     UPDATE activity
                         SET activity_state = 'ACTIVE',
                             activity_started_at = NOW(),
                             activity_updated_at = NOW()
                         FROM candidates
                         WHERE activity.activity_id = candidates.activity_id
                         RETURNING activity.*)
            SELECT updated.*,
                   process.process_business_key,
                   COALESCE(variables.variables_json, '[]'::json) AS variables_json
            FROM updated
                     JOIN process ON process.process_id = updated.process_id
            
                     LEFT JOIN LATERAL (
                SELECT json_agg(
                               jsonb_build_object(
                                       'id', variable.variable_id,
                                       'process_id', variable.process_id,
                                       'execution_id', variable.execution_id,
                                       'execution_definition_id', variable.execution_definition_id,
                                       'var_key', variable.variable_key,
                                       'var_value', variable.variable_value,
                                       'type', variable.variable_type
                               )
                       ) AS variables_json
                FROM variable
                WHERE variable.execution_id IN (updated.process_id, updated.activity_id)
                ) variables ON TRUE;
            """;

    private POLL() {
    }

}
