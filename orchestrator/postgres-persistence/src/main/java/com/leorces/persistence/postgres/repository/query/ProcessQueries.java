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
                   process.process_suspended,
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

    public static final String FIND_ALL_WITH_PAGINATION = """
            SELECT
                p.process_id,
                p.root_process_id,
                p.process_parent_id,
                p.process_definition_id,
                p.process_definition_key,
                p.process_business_key,
                p.process_state,
                p.process_suspended,
                p.process_created_at,
                p.process_updated_at,
                p.process_started_at,
                p.process_completed_at,
            
                d.definition_id,
                d.definition_key,
                d.definition_name,
                d.definition_version,
                d.definition_data
            FROM process p
            LEFT JOIN definition d ON p.process_definition_id = d.definition_id
            WHERE
                (
                    :filter IS NULL OR :filter = '' OR
                    p.process_id = :filter OR
                    p.process_definition_key = :filter OR
                    p.process_business_key = :filter
                )
                AND
                (
                    :state IS NULL OR :state = '' OR :state = 'all' OR p.process_state = :state
                )
            ORDER BY
                CASE WHEN :sort_by_field = 'created_at' AND :order = 'ASC' THEN p.process_created_at END ASC,
                CASE WHEN :sort_by_field = 'created_at' AND :order = 'DESC' THEN p.process_created_at END DESC,
                CASE WHEN :sort_by_field = 'updated_at' AND :order = 'ASC' THEN p.process_updated_at END ASC,
                CASE WHEN :sort_by_field = 'updated_at' AND :order = 'DESC' THEN p.process_updated_at END DESC,
                CASE WHEN :sort_by_field = 'started_at' AND :order = 'ASC' THEN p.process_started_at END ASC,
                CASE WHEN :sort_by_field = 'started_at' AND :order = 'DESC' THEN p.process_started_at END DESC,
                CASE WHEN :sort_by_field = 'completed_at' AND :order = 'ASC' THEN p.process_completed_at END ASC,
                CASE WHEN :sort_by_field = 'completed_at' AND :order = 'DESC' THEN p.process_completed_at END DESC,
                p.process_created_at DESC
            OFFSET :offset
            LIMIT :limit;
            """;

    public static final String COUNT_ALL_WITH_FILTERS = """
            SELECT COUNT(*)
            FROM process p
            LEFT JOIN definition d ON p.process_definition_id = d.definition_id
            WHERE (:filter IS NULL OR :filter = '' OR
                   p.process_id = :filter OR
                   p.process_definition_key = :filter OR
                   p.process_business_key = :filter OR
                   d.definition_name = :filter)
              AND (:state IS NULL OR :state = '' OR :state = 'all' OR p.process_state = :state)
            """;

    public static final String FIND_ALL_BY_FILTERS = BASE_SELECT + """
            WHERE (:processDefinitionKey IS NULL OR :processDefinitionKey = '' OR process.process_definition_key = :processDefinitionKey)
              AND (:processDefinitionId IS NULL OR :processDefinitionId = '' OR process.process_definition_id = :processDefinitionId)
              AND (:businessKey IS NULL OR :businessKey = '' OR process.process_business_key = :businessKey)
              AND (:processId IS NULL OR :processId = '' OR process.process_id = :processId)
              AND (
                    :variableKeys IS NULL OR :variableValues IS NULL OR :variableCount = 0
                    OR process.process_id IN (
                        SELECT variable.execution_id
                        FROM variable
                        WHERE variable.execution_id = process.process_id
                        GROUP BY variable.execution_id
                        HAVING COUNT(DISTINCT CASE
                                 WHEN variable.variable_key = ANY(:variableKeys)
                                  AND variable.variable_value::text = ANY(:variableValues)
                                 THEN variable.variable_key
                                 END) = :variableCount
                    )
                  )
              LIMIT 100;
            """;

    public static final String CHANGE_STATE = """
            UPDATE process
            SET process_state = :state,
                process_updated_at = NOW()
            WHERE process_id = :processId;
            """;

    public static final String RUN = """
            INSERT INTO process (
                process_id,
                root_process_id,
                process_parent_id,
                process_definition_id,
                process_definition_key,
                process_business_key,
                process_state,
                process_suspended,
                process_created_at,
                process_updated_at,
                process_started_at
            )
            SELECT
                :processId,
                :rootProcessId,
                :parentProcessId,
                :definitionId,
                :definitionKey,
                :businessKey,
                'ACTIVE',
                CASE
                    WHEN :suspended = TRUE THEN TRUE
                    ELSE COALESCE(ds.definition_suspended, FALSE)
                END,
                NOW(),
                NOW(),
                NOW()
            FROM (SELECT 1) v
            LEFT JOIN definition_suspended ds
                   ON ds.definition_id = :definitionId
            RETURNING *;
            """;

    public static final String COMPLETE = """
            UPDATE process
            SET process_state = 'COMPLETED',
                process_suspended = false,
                process_updated_at = NOW(),
                process_completed_at = NOW()
            WHERE process_id = :processId;
            """;

    public static final String TERMINATE = """
            UPDATE process
            SET process_state = 'TERMINATED',
                process_suspended = false,
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

    public static final String SUSPEND_BY_ID = """
            WITH RECURSIVE child_processes AS (
                SELECT process_id
                FROM process
                WHERE process_id = :processId
              UNION ALL
                SELECT p.process_id
                FROM process p
                INNER JOIN child_processes cp ON p.process_parent_id = cp.process_id
            )
            UPDATE process
            SET process_suspended = true,
                process_updated_at = NOW()
            WHERE process_id IN (SELECT process_id FROM child_processes);
            """;

    public static final String SUSPEND_BY_DEFINITION_ID = """
            WITH RECURSIVE child_processes AS (
                SELECT process_id
                FROM process
                WHERE process_definition_id = :definitionId AND process_suspended = false AND process_completed_at IS NULL
              UNION ALL
                SELECT p.process_id
                FROM process p
                INNER JOIN child_processes cp ON p.process_parent_id = cp.process_id
            )
            UPDATE process
            SET process_suspended = true,
                process_updated_at = NOW()
            WHERE process_id IN (SELECT process_id FROM child_processes);
            """;

    public static final String SUSPEND_BY_DEFINITION_KEY = """
            WITH RECURSIVE child_processes AS (
                SELECT process_id
                FROM process
                WHERE process_definition_key = :definitionKey AND process_suspended = false AND process_completed_at IS NULL
              UNION ALL
                SELECT p.process_id
                FROM process p
                INNER JOIN child_processes cp ON p.process_parent_id = cp.process_id
            )
            UPDATE process
            SET process_suspended = true,
                process_updated_at = NOW()
            WHERE process_id IN (SELECT process_id FROM child_processes);
            """;

    public static final String RESUME_BY_ID = """
            WITH RECURSIVE child_processes AS (
                SELECT process_id
                FROM process
                WHERE process_id = :processId
              UNION ALL
                SELECT p.process_id
                FROM process p
                INNER JOIN child_processes cp ON p.process_parent_id = cp.process_id
            )
            UPDATE process
            SET process_suspended = false,
                process_updated_at = NOW()
            WHERE process_id IN (SELECT process_id FROM child_processes)
              AND process_completed_at IS NULL;
            """;

    public static final String RESUME_BY_DEFINITION_ID = """
            WITH RECURSIVE child_processes AS (
                SELECT p.process_id,
                       p.process_definition_id,
                       p.process_parent_id
                FROM process p
                LEFT JOIN definition_suspended ds
                       ON ds.definition_id = p.process_definition_id
                WHERE p.process_definition_id = :definitionId
                  AND p.process_suspended = true
                  AND p.process_completed_at IS NULL
                  AND COALESCE(ds.definition_suspended, FALSE) = FALSE  -- фильтр по definition_suspended
              UNION ALL
                SELECT p.process_id,
                       p.process_definition_id,
                       p.process_parent_id
                FROM process p
                INNER JOIN child_processes cp ON p.process_parent_id = cp.process_id
                LEFT JOIN definition_suspended ds
                       ON ds.definition_id = p.process_definition_id
                WHERE COALESCE(ds.definition_suspended, FALSE) = FALSE  -- тоже фильтруем подпроцессы
            )
            UPDATE process
            SET process_suspended = false,
                process_updated_at = NOW()
            WHERE process_id IN (SELECT process_id FROM child_processes)
              AND process_completed_at IS NULL;
            """;

    public static final String RESUME_BY_DEFINITION_KEY = """
            WITH RECURSIVE child_processes AS (
                SELECT p.process_id,
                       p.process_definition_id,
                       p.process_parent_id
                FROM process p
                LEFT JOIN definition_suspended ds
                       ON ds.definition_id = p.process_definition_id
                WHERE p.process_definition_key = :definitionKey
                  AND p.process_suspended = true
                  AND p.process_completed_at IS NULL
                  AND COALESCE(ds.definition_suspended, FALSE) = FALSE
              UNION ALL
                SELECT p.process_id,
                       p.process_definition_id,
                       p.process_parent_id
                FROM process p
                INNER JOIN child_processes cp ON p.process_parent_id = cp.process_id
                LEFT JOIN definition_suspended ds
                       ON ds.definition_id = p.process_definition_id
                WHERE COALESCE(ds.definition_suspended, FALSE) = FALSE
            )
            UPDATE process
            SET process_suspended = false,
                process_updated_at = NOW()
            WHERE process_id IN (SELECT process_id FROM child_processes)
              AND process_completed_at IS NULL;
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
                   process.process_suspended,
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
            WHERE process.process_state NOT IN ('ACTIVE', 'INCIDENT')
              AND (process.root_process_id IS NULL OR NOT EXISTS (
                  SELECT 1 FROM process root_process
                  WHERE root_process.process_id = process.root_process_id
                    AND root_process.process_state IN ('ACTIVE', 'INCIDENT')
                  LIMIT 1
              ))
              AND NOT EXISTS (
                  SELECT 1 FROM activity
                  WHERE activity.process_id = process.process_id
                    AND activity.activity_completed_at IS NULL
                  LIMIT 1
              )
            ORDER BY process.process_completed_at ASC
            LIMIT :limit
            """;

    private ProcessQueries() {
        // Utility class
    }

}