package com.leorces.persistence.postgres.repository.query.process;

public class FIND_EXECUTION_BY_ID {

    public static final String FIND_EXECUTION_BY_ID_QUERY = """
            SELECT process.process_id,
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
                   COALESCE(acts.activities_json, '[]'::json)     AS activities_json
            FROM process
                     LEFT JOIN definition
                               ON process.process_definition_id = definition.definition_id
            
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
                  AND a.activity_state != 'DELETED'
                ) AS acts ON TRUE
            WHERE process.process_id = :processId;
            """;

    private FIND_EXECUTION_BY_ID() {
        // Index: pk_process
    }

}
