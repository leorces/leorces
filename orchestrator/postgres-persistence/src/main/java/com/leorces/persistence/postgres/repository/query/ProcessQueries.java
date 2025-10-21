package com.leorces.persistence.postgres.repository.query;

public final class ProcessQueries {

    public static final String BASE_SELECT = """
            SELECT
                   process.process_id,
                   process.root_process_id,
                   process.process_parent_id,
                   process.process_definition_id,
                   process.process_definition_key,
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
                   definition.definition_data,
            
                   (SELECT COALESCE(json_agg(json_build_object(
                       'id', variable.variable_id,
                       'process_id', variable.process_id,
                       'execution_id', variable.execution_id,
                       'execution_definition_id', variable.execution_definition_id,
                       'var_key', variable.variable_key,
                       'var_value', variable.variable_value,
                       'type', variable.variable_type
                   )), '[]'::json)
                   FROM variable
                   WHERE variable.execution_id = process.process_id) variables_json
            FROM process
            LEFT JOIN definition ON process.process_definition_id = definition.definition_id
            """;

    public static final String FIND_BY_ID = BASE_SELECT + """
            WHERE process.process_id = :processId
            """;

    public static final String FIND_ALL_WITH_PAGINATION = BASE_SELECT + """
            WHERE (:filter IS NULL OR :filter = '' OR
                   LOWER(process.process_id) LIKE LOWER(CONCAT('%', :filter, '%')) OR
                   LOWER(process.process_definition_key) LIKE LOWER(CONCAT('%', :filter, '%')) OR
                   LOWER(process.process_business_key) LIKE LOWER(CONCAT('%', :filter, '%')) OR
                   LOWER(definition.definition_name) LIKE LOWER(CONCAT('%', :filter, '%')))
              AND (:state IS NULL OR :state = '' OR :state = 'all' OR process.process_state = :state)
            ORDER BY
                CASE WHEN :sort_by_field = 'created_at' AND :order = 'ASC' THEN process.process_created_at END ASC,
                CASE WHEN :sort_by_field = 'created_at' AND :order = 'DESC' THEN process.process_created_at END DESC,
                CASE WHEN :sort_by_field = 'updated_at' AND :order = 'ASC' THEN process.process_updated_at END ASC,
                CASE WHEN :sort_by_field = 'updated_at' AND :order = 'DESC' THEN process.process_updated_at END DESC,
                CASE WHEN :sort_by_field = 'started_at' AND :order = 'ASC' THEN process.process_started_at END ASC,
                CASE WHEN :sort_by_field = 'started_at' AND :order = 'DESC' THEN process.process_started_at END DESC,
                CASE WHEN :sort_by_field = 'completed_at' AND :order = 'ASC' THEN process.process_completed_at END ASC,
                CASE WHEN :sort_by_field = 'completed_at' AND :order = 'DESC' THEN process.process_completed_at END DESC,
                process.process_created_at DESC
            OFFSET :offset
            LIMIT :limit
            """;

    public static final String FIND_ALL_BY_BUSINESS_KEY = BASE_SELECT + """
            WHERE process.process_business_key = :businessKey
            """;

    public static final String COUNT_ALL_WITH_FILTERS = """
            SELECT COUNT(*)
            FROM process
            LEFT JOIN definition ON process.process_definition_id = definition.definition_id
            WHERE (:filter IS NULL OR :filter = '' OR
                   LOWER(process.process_id) LIKE LOWER(CONCAT('%', :filter, '%')) OR
                   LOWER(process.process_definition_key) LIKE LOWER(CONCAT('%', :filter, '%')) OR
                   LOWER(process.process_business_key) LIKE LOWER(CONCAT('%', :filter, '%')) OR
                   LOWER(definition.definition_name) LIKE LOWER(CONCAT('%', :filter, '%')))
              AND (:state IS NULL OR :state = '' OR :state = 'all' OR process.process_state = :state)
            """;

    public static final String FIND_BY_VARIABLES = BASE_SELECT + """
            WHERE process.process_id IN (
                SELECT DISTINCT variable.execution_id
                FROM variable
                WHERE variable.execution_id = process.process_id
                GROUP BY variable.execution_id
                HAVING COUNT(DISTINCT CASE WHEN variable.variable_key = ANY(:variableKeys) AND variable.variable_value::text = ANY(:variableValues) THEN variable.variable_key END) = :variableCount
            )
            """;

    public static final String FIND_BY_BUSINESS_KEY_AND_VARIABLES = BASE_SELECT + """
            WHERE process.process_business_key = :businessKey
              AND process.process_id IN (
                SELECT DISTINCT variable.execution_id
                FROM variable
                WHERE variable.execution_id = process.process_id
                GROUP BY variable.execution_id
                HAVING COUNT(DISTINCT CASE WHEN variable.variable_key = ANY(:variableKeys) AND variable.variable_value::text = ANY(:variableValues) THEN variable.variable_key END) = :variableCount
              )
            """;

    public static final String CHANGE_STATE = """
            UPDATE process
            SET process_state = :state,
                process_updated_at = NOW()
            WHERE process_id = :processId;
            """;

    private static final String BASE_SELECT_WITH_ACTIVITIES = """
            SELECT
                   process.process_id,
                   process.root_process_id,
                   process.process_parent_id,
                   process.process_definition_id,
                   process.process_definition_key,
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
                   WHERE variable.execution_id = process.process_id) variables_json,
            
                   (SELECT COALESCE(json_agg(json_build_object(
                       'id', activity.activity_id,
                       'process_id', activity.process_id,
                       'activity_definition_id', activity.activity_definition_id,
                       'parent_activity_definition_id', activity.activity_parent_definition_id,
                       'process_definition_id', activity.process_definition_id,
                       'process_definition_key', activity.process_definition_key,
                       'type', activity.activity_type,
                       'state', activity.activity_state,
                       'retries', activity.activity_retries,
                       'activity_timeout', activity.activity_timeout,
                       'activity_failure_reason', activity.activity_failure_reason,
                       'activity_failure_trace', activity.activity_failure_trace,
                       'created_at', activity.activity_created_at,
                       'updated_at', activity.activity_updated_at,
                       'started_at', activity.activity_started_at,
                       'completed_at', activity.activity_completed_at,
                       'async', activity.activity_async,
                       'variablesJson', (SELECT COALESCE(json_agg(json_build_object(
                           'id', activity_variable.variable_id,
                           'process_id', activity_variable.process_id,
                           'execution_id', activity_variable.execution_id,
                           'execution_definition_id', activity_variable.execution_definition_id,
                           'var_key', activity_variable.variable_key,
                           'var_value', activity_variable.variable_value,
                           'type', activity_variable.variable_type,
                           'created_at', activity_variable.variable_created_at,
                           'updated_at', activity_variable.variable_updated_at
                       )), '[]'::json)
                       FROM variable activity_variable
                       WHERE activity_variable.execution_id = activity.activity_id)
                   )), '[]'::json)
                   FROM activity
                   WHERE activity.process_id = process.process_id) activities_json
            FROM process
            LEFT JOIN definition ON process.process_definition_id = definition.definition_id
            """;

    public static final String FIND_BY_ID_WITH_ACTIVITIES = BASE_SELECT_WITH_ACTIVITIES + """
            WHERE process.process_id = :processId
            """;

    public static final String FIND_ALL_FULLY_COMPLETED = BASE_SELECT_WITH_ACTIVITIES + """
            WHERE process.process_state != 'ACTIVE'
              AND process.process_state != 'INCIDENT'
              AND (process.root_process_id IS NULL OR NOT EXISTS (
                  SELECT 1
                  FROM process root_process
                  WHERE root_process.process_id = process.root_process_id
                    AND (root_process.process_state = 'ACTIVE'
                         OR root_process.process_state = 'INCIDENT')
              ))
              AND NOT EXISTS (
                  SELECT 1
                  FROM activity activity_check
                  WHERE activity_check.process_id = process.process_id
                    AND activity_check.activity_completed_at IS NULL
              )
            LIMIT :limit
            """;

    private ProcessQueries() {
        // Utility class
    }

}