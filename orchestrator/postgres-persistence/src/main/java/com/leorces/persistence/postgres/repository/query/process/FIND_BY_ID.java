package com.leorces.persistence.postgres.repository.query.process;


public class FIND_BY_ID {

    public static final String FIND_BY_ID_QUERY = """
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
                   definition.definition_suspended,
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
                  AND v.execution_definition_id = process.process_definition_id
                ) AS variables ON TRUE
            WHERE process.process_id = :processId
            """;

    private FIND_BY_ID() {
        // Index: pk_process
    }

}
