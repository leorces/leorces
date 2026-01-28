package com.leorces.persistence.postgres.repository.query.process;

public class FIND_EXECUTIONS_FOR_UPDATE {

    public static final String FIND_EXECUTIONS_FOR_UPDATE_QUERY = """
            WITH locked_processes AS (SELECT *
                                      FROM process
                                      WHERE process_definition_id = :definitionId
                                          FOR UPDATE SKIP LOCKED
                                      LIMIT :limit)
            SELECT lp.process_id,
                   lp.root_process_id,
                   lp.process_parent_id,
                   lp.process_definition_id,
                   lp.process_definition_key,
                   lp.process_business_key,
                   lp.process_state,
                   lp.process_suspended,
                   lp.process_created_at,
                   lp.process_updated_at,
                   lp.process_started_at,
                   lp.process_completed_at,
            
                   d.definition_id,
                   d.definition_key,
                   d.definition_name,
                   d.definition_version,
                   d.definition_schema,
                   d.definition_origin,
                   d.definition_deployment,
                   d.definition_created_at,
                   d.definition_updated_at,
                   d.definition_data,
            
                   COALESCE(proc_vars.variables_json, '[]'::json) AS variables_json,
                   COALESCE(acts.activities_json, '[]'::json)     AS activities_json
            FROM locked_processes lp
                     LEFT JOIN definition d
                               ON lp.process_definition_id = d.definition_id
            
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
                WHERE v.execution_id = lp.process_id
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
                WHERE a.process_id = lp.process_id
                  AND a.activity_state != 'DELETED'
                ) AS acts ON TRUE;
            """;

    private FIND_EXECUTIONS_FOR_UPDATE() {
    }

}
