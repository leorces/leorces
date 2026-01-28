package com.leorces.persistence.postgres.repository.query.process;

public class BATCH_UPDATE_DEFINITION_ID {

    public static final String BATCH_UPDATE_DEFINITION_ID_QUERY = """
            WITH batch AS (SELECT process_id
                           FROM process
                           WHERE process_definition_id = :fromDefinitionId
                           ORDER BY process_created_at ASC
                               FOR UPDATE SKIP LOCKED
                           LIMIT :limit)
            UPDATE process
            SET process_definition_id = :toDefinitionId,
                process_updated_at    = NOW()
            WHERE process_id IN (SELECT process_id FROM batch)
            RETURNING process_id;
            """;

    private BATCH_UPDATE_DEFINITION_ID() {
    }

}
