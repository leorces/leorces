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
            )
            """;

    public static final String IS_ALL_COMPLETED_BY_PROCESS_ID = """
            SELECT NOT EXISTS (
                SELECT 1
                FROM activity
                WHERE process_id = :processId
                  AND activity_state NOT IN ('COMPLETED', 'TERMINATED')
                  AND activity_async = false
            );
            """;

    public static final String IS_ALL_COMPLETED = """
            SELECT NOT EXISTS (
                SELECT 1
                FROM activity
                WHERE process_id = :processId
                  AND activity_definition_id IN (:definitionIds)
                  AND activity_state NOT IN ('COMPLETED', 'TERMINATED')
            );
            """;

    public static final String UPDATE_STATUS_BATCH = """
            UPDATE activity
            SET activity_state = :newState,
                activity_updated_at = CURRENT_TIMESTAMP,
                activity_started_at = CASE WHEN :newState = 'ACTIVE' THEN CURRENT_TIMESTAMP ELSE activity_started_at END
            WHERE activity_id IN (:activityIds)
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
                   process.process_created_at,
                   process.process_updated_at,
                   process.process_started_at,
                   process.process_completed_at,
            
                   definition.definition_id,
                   definition.definition_key,
                   definition.definition_name,
                   definition.definition_version,
                   definition.definition_schema,
                   definition.definition_origin,
                   definition.definition_deployment,
                   definition.definition_created_at,
                   definition.definition_updated_at,
                   definition.definition_data,
            
                   (SELECT COALESCE(json_agg(json_build_object(
                       'id', variable.variable_id,
                       'process_id', variable.process_id,
                       'execution_id', variable.execution_id,
                       'execution_definition_id', variable.execution_definition_id,
                       'var_key', variable.variable_key,
                       'var_value', variable.variable_value,
                       'type', variable.variable_type,
                       'created_at', variable.variable_created_at,
                       'updated_at', variable.variable_updated_at
                   )), '[]'::json)
                   FROM variable
                   WHERE variable.process_id = activity.process_id) variables_json
            FROM activity
            LEFT JOIN process ON activity.process_id = process.process_id
            LEFT JOIN definition ON activity.process_definition_id = definition.definition_id
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
              AND (activity.activity_state = 'ACTIVE' OR activity.activity_state = 'SCHEDULED')
            """;

    public static final String FIND_ALL_ACTIVE_BY_PROCESS_ID = BASE_SELECT + """
            WHERE activity.process_id = :processId
              AND (activity.activity_state = 'ACTIVE' OR activity.activity_state = 'SCHEDULED')
            """;

    public static final String FIND_ALL_FAILED_BY_PROCESS_ID = BASE_SELECT + """
                    WHERE activity.process_id = :processId
                      AND (activity.activity_state = 'FAILED')
            """;

    public static final String FIND_ALL_BY_IDS = BASE_SELECT + """
            WHERE activity.activity_id IN (:activityIds)
            """;

    private ActivityQueries() {
        // Utility class
    }

}