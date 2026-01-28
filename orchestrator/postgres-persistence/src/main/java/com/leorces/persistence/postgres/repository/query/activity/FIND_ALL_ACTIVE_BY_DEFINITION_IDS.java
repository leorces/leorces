package com.leorces.persistence.postgres.repository.query.activity;

public class FIND_ALL_ACTIVE_BY_DEFINITION_IDS {

    public static final String FIND_ALL_ACTIVE_BY_DEFINITION_IDS_QUERY = """
            SELECT activity.activity_id,
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
                   activity.activity_created_at,
                   activity.activity_updated_at,
                   activity.activity_started_at,
                   activity.activity_completed_at,
            
                   process.root_process_id,
                   process.process_parent_id,
                   process.process_business_key,
                   process.process_state,
                   process.process_suspended,
            
                   definition.definition_id,
                   definition.definition_key,
                   definition.definition_version,
                   definition.definition_data
            FROM activity
                     LEFT JOIN process ON activity.process_id = process.process_id
                     LEFT JOIN definition ON activity.process_definition_id = definition.definition_id
            WHERE activity.process_id = :processId
              AND activity.activity_definition_id = ANY (:definitionIds)
              AND activity_state IN ('ACTIVE', 'SCHEDULED', 'FAILED');
            """;

    private FIND_ALL_ACTIVE_BY_DEFINITION_IDS() {
    }

}
