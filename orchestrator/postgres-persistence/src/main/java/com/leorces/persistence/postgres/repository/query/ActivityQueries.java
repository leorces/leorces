package com.leorces.persistence.postgres.repository.query;

public final class ActivityQueries {

    public static final String DELETE_ALL_ACTIVE_BY_DEFINITION_IDS = """
            DELETE FROM activity
            WHERE process_id = :processId
              AND activity_definition_id = ANY(:definitionIds)
              AND activity_state IN ('ACTIVE', 'SCHEDULED');
            """;

    public static final String CHANGE_STATE = """
            UPDATE activity
            SET activity_state = :state,
                activity_updated_at = NOW()
            WHERE activity_id = :activityId;
            """;

    public static final String IS_ANY_FAILED = """
            SELECT EXISTS (
                SELECT 1 FROM activity
                WHERE process_id = :processId AND activity_state = 'FAILED'
                LIMIT 1
            )
            """;

    public static final String IS_ALL_COMPLETED_BY_DEFINITION_ID = """
            SELECT NOT EXISTS (
                SELECT 1 FROM activity
                WHERE process_id = :processId
                  AND activity_parent_definition_id = :definitionId
                  AND activity_completed_at IS NULL
                LIMIT 1
            );
            """;

    public static final String IS_ALL_COMPLETED_BY_PROCESS_ID = """
            SELECT NOT EXISTS (
                SELECT 1 FROM activity
                WHERE process_id = :processId
                  AND activity_async = false
                  AND activity_completed_at IS NULL
                LIMIT 1
            );
            """;

    public static final String IS_ALL_COMPLETED = """
            SELECT NOT EXISTS (
                SELECT 1 FROM activity
                WHERE process_id = :processId
                  AND activity_definition_id = ANY(:definitionIds)
                  AND activity_completed_at IS NULL
                LIMIT 1
            );
            """;

    public static final String POLL = """
            WITH updated AS (
                UPDATE activity
                SET activity_state = 'ACTIVE',
                    activity_started_at = NOW(),
                    activity_updated_at = NOW()
                WHERE activity_id IN (
                    SELECT activity_id
                    FROM activity
                    WHERE activity_topic = :topic
                      AND activity_state = 'SCHEDULED'
                      AND process_definition_key = :processDefinitionKey
                    ORDER BY activity_created_at ASC
                    LIMIT :limit
                    FOR UPDATE SKIP LOCKED
                )
                RETURNING *
            ),
            variables_data AS (
                SELECT execution_id,
                       json_agg(json_build_object(
                           'id', variable_id,
                           'process_id', process_id,
                           'execution_id', execution_id,
                           'execution_definition_id', execution_definition_id,
                           'var_key', variable_key,
                           'var_value', variable_value,
                           'type', variable_type
                       )) AS variables_json
                FROM variable
                WHERE execution_id IN (SELECT process_id FROM updated)
                GROUP BY execution_id
            )
            SELECT updated.*, COALESCE(variables_data.variables_json, '[]'::json) AS variables_json
            FROM updated
            LEFT JOIN variables_data ON variables_data.execution_id = updated.process_id;
            """;

    private static final String BASE_SELECT = """
            SELECT
                   activity.activity_id,
                   activity.process_id,
                   activity.activity_definition_id,
                   activity.activity_parent_definition_id,
                   activity.process_definition_id,
                   activity.process_definition_key,
                   activity.activity_type,
                   activity.activity_state,
                   activity.activity_retries,
                   activity.activity_timeout,
                   activity.activity_failure_reason,
                   activity.activity_failure_trace,
                   activity.activity_async,
                   activity.activity_created_at,
                   activity.activity_updated_at,
                   activity.activity_started_at,
                   activity.activity_completed_at,
            
                   process.root_process_id,
                   process.process_parent_id,
                   process.process_business_key,
                   process.process_state,
            
                   definition.definition_id,
                   definition.definition_key,
                   definition.definition_version,
                   definition.definition_data
            FROM activity
            LEFT JOIN process ON activity.process_id = process.process_id
            LEFT JOIN definition ON activity.process_definition_id = definition.definition_id
            """;

    public static final String FIND_ALL_ACTIVE_BY_DEFINITION_IDS = BASE_SELECT + """
            WHERE activity.process_id = :processId
              AND activity.activity_definition_id = ANY(:definitionIds)
              AND activity.activity_completed_at IS NULL
            """;

    public static final String FIND_ALL_ACTIVE_BY_PROCESS_ID = BASE_SELECT + """
            WHERE activity.process_id = :processId
              AND activity.activity_completed_at IS NULL
            """;

    public static final String FIND_ALL_FAILED_BY_PROCESS_ID = BASE_SELECT + """
            WHERE activity.process_id = :processId
              AND activity.activity_state = 'FAILED'
            """;

    public static final String FIND_TIMED_OUT = BASE_SELECT + """
            WHERE activity.activity_timeout < CURRENT_TIMESTAMP
              AND activity.activity_state IN ('ACTIVE', 'SCHEDULED')
            ORDER BY activity.activity_timeout
            LIMIT :limit;
            """;

    private static final String FULL_SELECT = """
            SELECT
                   activity.activity_id,
                   activity.process_id,
                   activity.activity_definition_id,
                   activity.activity_parent_definition_id,
                   activity.process_definition_id,
                   activity.process_definition_key,
                   activity.activity_type,
                   activity.activity_state,
                   activity.activity_retries,
                   activity.activity_timeout,
                   activity.activity_failure_reason,
                   activity.activity_failure_trace,
                   activity.activity_async,
                   activity.activity_created_at,
                   activity.activity_updated_at,
                   activity.activity_started_at,
                   activity.activity_completed_at,
            
                   process.root_process_id,
                   process.process_parent_id,
                   process.process_business_key,
                   process.process_state,
            
                   definition.definition_id,
                   definition.definition_key,
                   definition.definition_version,
                   definition.definition_data,
            
                   COALESCE(variables.variables_json, '[]'::json) AS variables_json
            FROM activity
            LEFT JOIN process ON activity.process_id = process.process_id
            LEFT JOIN definition ON activity.process_definition_id = definition.definition_id
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
                WHERE variable.execution_id = activity.process_id
            ) AS variables ON TRUE
            """;

    public static final String FIND_BY_ID = FULL_SELECT + """
            WHERE activity.activity_id = :activityId
            """;

    public static final String FIND_BY_DEFINITION_ID = FULL_SELECT + """
            WHERE activity.process_id = :processId
              AND activity.activity_definition_id = :definitionId
            """;

    private ActivityQueries() {
        // Utility class
    }

}