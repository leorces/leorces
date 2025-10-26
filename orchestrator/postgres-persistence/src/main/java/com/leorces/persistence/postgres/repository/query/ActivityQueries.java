package com.leorces.persistence.postgres.repository.query;

public final class ActivityQueries {

    public static final String DELETE_ALL_ACTIVE_BY_DEFINITION_IDS = """
            DELETE FROM activity
                WHERE process_id = :processId
                  AND activity_definition_id IN (:definitionIds)
                  AND activity_state in ('ACTIVE', 'SCHEDULED');
            """;

    public static final String CHANGE_STATE = """
            UPDATE activity
            SET activity_state = :state,
                activity_updated_at = NOW()
            WHERE activity_id = :activityId;
            """;

    public static final String IS_ANY_FAILED = """
            SELECT EXISTS (
                SELECT 1
                FROM activity
                WHERE process_id = :processId
                  AND activity_state = 'FAILED'
            )
            """;

    public static final String IS_ALL_COMPLETED_BY_DEFINITION_ID = """
            SELECT NOT EXISTS (
                SELECT 1
                FROM activity
                WHERE process_id = :processId
                  AND activity_parent_definition_id = :definitionId
                  AND activity_completed_at IS NULL
            );
            """;

    public static final String IS_ALL_COMPLETED_BY_PROCESS_ID = """
            SELECT NOT EXISTS (
                SELECT 1
                FROM activity
                WHERE process_id = :processId
                  AND activity_async = false
                  AND activity_completed_at IS NULL
            );
            """;

    public static final String IS_ALL_COMPLETED = """
            SELECT NOT EXISTS (
                SELECT 1
                FROM activity
                WHERE process_id = :processId
                  AND activity_definition_id IN (:definitionIds)
                  AND activity_completed_at IS NULL
            );
            """;

    public static final String POLL = """
            WITH polled AS (
                    SELECT activity_id,
                           process_id,
                           activity_definition_id,
                           activity_parent_definition_id,
                           process_definition_id,
                           process_definition_key,
                           activity_type,
                           activity_state,
                           activity_retries,
                           activity_timeout,
                           activity_failure_reason,
                           activity_failure_trace,
                           activity_async,
                           activity_created_at,
                           activity_updated_at,
                           activity_started_at,
                           activity_completed_at
                    FROM activity
                    WHERE activity_topic = :topic
                      AND activity_state = 'SCHEDULED'
                      AND process_definition_key = :processDefinitionKey
                    ORDER BY activity_created_at ASC
                    LIMIT :limit
                    FOR UPDATE SKIP LOCKED
                )
                SELECT
                    polled.*,
                    COALESCE(variables.variables_json, '[]'::json) AS variables_json
                FROM polled
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
                    WHERE variable.execution_id = polled.process_id
                ) AS variables ON TRUE;
            """;

    private static final String BASE_SELECT = """
            SELECT DISTINCT ON (activity.activity_id)
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

    public static final String FIND_BY_ID = BASE_SELECT + """
            WHERE activity.activity_id = :activityId
            """;

    public static final String FIND_BY_DEFINITION_ID = BASE_SELECT + """
            WHERE activity.process_id = :processId
              AND activity.activity_definition_id = :definitionId
            """;

    public static final String FIND_ALL_ACTIVE_BY_DEFINITION_IDS = BASE_SELECT + """
            WHERE activity.process_id = :processId
              AND activity.activity_definition_id IN (:definitionIds)
              AND activity_completed_at IS NULL
            """;

    public static final String FIND_ALL_ACTIVE_BY_PROCESS_ID = BASE_SELECT + """
            WHERE activity.process_id = :processId
              AND activity_completed_at IS NULL
            """;

    public static final String FIND_ALL_FAILED_BY_PROCESS_ID = BASE_SELECT + """
            WHERE activity.process_id = :processId
              AND (activity.activity_state = 'FAILED')
            """;

    public static final String FIND_TIMED_OUT = BASE_SELECT + """
            WHERE (activity.activity_state = 'ACTIVE' OR activity.activity_state = 'SCHEDULED')
              AND activity.activity_timeout IS NOT NULL
              AND activity.activity_timeout < CURRENT_TIMESTAMP
            LIMIT :limit
            """;

    private ActivityQueries() {
        // Utility class
    }

}