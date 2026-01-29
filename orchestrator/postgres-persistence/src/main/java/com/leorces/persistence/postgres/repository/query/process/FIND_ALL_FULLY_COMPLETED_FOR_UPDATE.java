package com.leorces.persistence.postgres.repository.query.process;

public class FIND_ALL_FULLY_COMPLETED_FOR_UPDATE {

    public static final String FIND_ALL_FULLY_COMPLETED_FOR_UPDATE_QUERY = """
            WITH eligible_processes AS (SELECT process.*
                                        FROM process
                                        WHERE process.process_state IN ('COMPLETED', 'TERMINATED', 'DELETED')
                                          AND (
                                            process.root_process_id IS NULL
                                                OR NOT EXISTS (SELECT 1
                                                               FROM process rp
                                                               WHERE rp.process_id = process.root_process_id
                                                                 AND rp.process_state IN ('ACTIVE', 'INCIDENT'))
                                            )
                                          AND NOT EXISTS (SELECT 1
                                                          FROM activity
                                                          WHERE activity.process_id = process.process_id
                                                            AND activity.activity_state NOT IN ('COMPLETED', 'TERMINATED', 'DELETED'))
                                            FOR UPDATE SKIP LOCKED)
            SELECT eligible_processes.process_id,
                   eligible_processes.root_process_id,
                   eligible_processes.process_parent_id,
                   eligible_processes.process_definition_id,
                   eligible_processes.process_definition_key,
                   eligible_processes.process_business_key,
                   eligible_processes.process_state,
                   eligible_processes.process_suspended,
                   eligible_processes.process_created_at,
                   eligible_processes.process_updated_at,
                   eligible_processes.process_started_at,
                   eligible_processes.process_completed_at,
            
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
            
                   COALESCE(process_variables.variables_json, '[]'::json)    AS variables_json,
                   COALESCE(activity_aggregates.activities_json, '[]'::json) AS activities_json
            FROM eligible_processes
                     LEFT JOIN definition
                               ON definition.definition_id = eligible_processes.process_definition_id
                     LEFT JOIN LATERAL (
                SELECT json_agg(
                               jsonb_build_object(
                                       'id', variable.variable_id,
                                       'process_id', variable.process_id,
                                       'execution_id', variable.execution_id,
                                       'execution_definition_id', variable.execution_definition_id,
                                       'var_key', variable.variable_key,
                                       'var_value', variable.variable_value,
                                       'type', variable.variable_type,
                                       'created_at', variable.variable_created_at,
                                       'updated_at', variable.variable_updated_at
                               )
                       ) AS variables_json
                FROM variable
                WHERE variable.execution_id = eligible_processes.process_id
                ) process_variables ON TRUE
                     LEFT JOIN LATERAL (
                SELECT json_agg(
                               jsonb_build_object(
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
                                       'variablesJson', COALESCE(activity_variables.variables_json, '[]'::json)
                               )
                       ) AS activities_json
                FROM activity
                         LEFT JOIN LATERAL (
                    SELECT json_agg(
                                   jsonb_build_object(
                                           'id', variable.variable_id,
                                           'process_id', variable.process_id,
                                           'execution_id', variable.execution_id,
                                           'execution_definition_id', variable.execution_definition_id,
                                           'var_key', variable.variable_key,
                                           'var_value', variable.variable_value,
                                           'type', variable.variable_type,
                                           'created_at', variable.variable_created_at,
                                           'updated_at', variable.variable_updated_at
                                   )
                           ) AS variables_json
                    FROM variable
                    WHERE variable.execution_id = activity.activity_id
                    ) activity_variables ON TRUE
                WHERE activity.process_id = eligible_processes.process_id
                ) activity_aggregates ON TRUE
            ORDER BY eligible_processes.process_completed_at
            LIMIT :limit;
            """;

    private FIND_ALL_FULLY_COMPLETED_FOR_UPDATE() {
        // Index: idx_process_eligible
    }

}
