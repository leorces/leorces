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
            
                   COALESCE(variables.variables_json, '[]'::json) AS variables_json
            FROM process
            LEFT JOIN definition ON process.process_definition_id = definition.definition_id
            LEFT JOIN LATERAL (
                SELECT json_agg(
                           jsonb_build_object(
                               'id', v.variable_id,
                               'process_id', v.process_id,
                               'execution_id', v.execution_id,
                               'execution_definition_id', v.execution_definition_id,
                               'var_key', v.variable_key,
                               'var_value', v.variable_value,
                               'type', v.variable_type
                           )
                       ) AS variables_json
                FROM variable v
                WHERE v.execution_id = process.process_id
            ) AS variables ON TRUE
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
                HAVING COUNT(DISTINCT CASE WHEN variable.variable_key = ANY(:variableKeys)
                                            AND variable.variable_value::text = ANY(:variableValues)
                                            THEN variable.variable_key END) = :variableCount
            )
            """;

    public static final String FIND_BY_BUSINESS_KEY_AND_VARIABLES = BASE_SELECT + """
            WHERE process.process_business_key = :businessKey
              AND process.process_id IN (
                SELECT DISTINCT variable.execution_id
                FROM variable
                WHERE variable.execution_id = process.process_id
                GROUP BY variable.execution_id
                HAVING COUNT(DISTINCT CASE WHEN variable.variable_key = ANY(:variableKeys)
                                            AND variable.variable_value::text = ANY(:variableValues)
                                            THEN variable.variable_key END) = :variableCount
              )
            """;

    public static final String CHANGE_STATE = """
            UPDATE process
            SET process_state = :state,
                process_updated_at = NOW()
            WHERE process_id = :processId;
            """;

    public static final String COMPLETE = """
            UPDATE process
            SET process_state = 'COMPLETED',
                process_updated_at = NOW(),
                process_completed_at = NOW()
            WHERE process_id = :processId;
            """;

    public static final String TERMINATE = """
            UPDATE process
            SET process_state = 'TERMINATED',
                process_updated_at = NOW(),
                process_completed_at = NOW()
            WHERE process_id = :processId;
            """;

    public static final String INCIDENT = """
            UPDATE process
            SET process_state = 'INCIDENT',
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
            
                   COALESCE(proc_vars.variables_json, '[]'::json) AS variables_json,
                   COALESCE(acts.activities_json, '[]'::json) AS activities_json
            FROM process
            LEFT JOIN definition ON process.process_definition_id = definition.definition_id
            LEFT JOIN LATERAL (
                SELECT json_agg(
                           jsonb_build_object(
                               'id', v.variable_id,
                               'process_id', v.process_id,
                               'execution_id', v.execution_id,
                               'execution_definition_id', v.execution_definition_id,
                               'var_key', v.variable_key,
                               'var_value', v.variable_value,
                               'type', v.variable_type,
                               'created_at', v.variable_created_at,
                               'updated_at', v.variable_updated_at
                           )
                       ) AS variables_json
                FROM variable v
                WHERE v.execution_id = process.process_id
            ) AS proc_vars ON TRUE
            LEFT JOIN LATERAL (
                SELECT json_agg(
                           jsonb_build_object(
                               'id', a.activity_id,
                               'process_id', a.process_id,
                               'activity_definition_id', a.activity_definition_id,
                               'parent_activity_definition_id', a.activity_parent_definition_id,
                               'process_definition_id', a.process_definition_id,
                               'process_definition_key', a.process_definition_key,
                               'type', a.activity_type,
                               'state', a.activity_state,
                               'retries', a.activity_retries,
                               'activity_timeout', a.activity_timeout,
                               'activity_failure_reason', a.activity_failure_reason,
                               'activity_failure_trace', a.activity_failure_trace,
                               'created_at', a.activity_created_at,
                               'updated_at', a.activity_updated_at,
                               'started_at', a.activity_started_at,
                               'completed_at', a.activity_completed_at,
                               'async', a.activity_async,
                               'variablesJson', COALESCE(act_vars.variables_json, '[]'::json)
                           )
                       ) AS activities_json
                FROM activity a
                LEFT JOIN LATERAL (
                    SELECT json_agg(
                               jsonb_build_object(
                                   'id', av.variable_id,
                                   'process_id', av.process_id,
                                   'execution_id', av.execution_id,
                                   'execution_definition_id', av.execution_definition_id,
                                   'var_key', av.variable_key,
                                   'var_value', av.variable_value,
                                   'type', av.variable_type,
                                   'created_at', av.variable_created_at,
                                   'updated_at', av.variable_updated_at
                               )
                           ) AS variables_json
                    FROM variable av
                    WHERE av.execution_id = a.activity_id
                ) AS act_vars ON TRUE
                WHERE a.process_id = process.process_id
            ) AS acts ON TRUE
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
