package com.leorces.persistence.postgres.repository.query.process;

public class FIND_ALL_BY_FILTERS {

    public static final String FIND_ALL_BY_FILTERS_QUERY = """
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
            WHERE (:processDefinitionKey IS NULL OR :processDefinitionKey = '' OR
                   process.process_definition_key = :processDefinitionKey)
              AND (:processDefinitionId IS NULL OR :processDefinitionId = '' OR
                   process.process_definition_id = :processDefinitionId)
              AND (:businessKey IS NULL OR :businessKey = '' OR process.process_business_key = :businessKey)
              AND (:processId IS NULL OR :processId = '' OR process.process_id = :processId)
              AND (
                :variableKeys IS NULL OR :variableValues IS NULL OR :variableCount = 0
                    OR process.process_id IN (SELECT v.execution_id
                                              FROM variable v
                                              WHERE v.execution_id = process.process_id
                                                AND v.variable_key = ANY (:variableKeys)
                                                AND v.variable_value = ANY (:variableValues)
                                              GROUP BY v.execution_id
                                              HAVING COUNT(DISTINCT v.variable_key) = :variableCount)
                )
            LIMIT 100;
            """;

    private FIND_ALL_BY_FILTERS() {
    }

}
