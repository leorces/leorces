package com.leorces.persistence.postgres.repository.query.process;

public class UPDATE_DEFINITION_ID {

    public static final String UPDATE_DEFINITION_ID_QUERY = """
            UPDATE process
            SET process_definition_id = :toDefinitionId,
                process_updated_at    = NOW()
            WHERE process_id = ANY (:processIds)
            RETURNING process_id;
            """;

    private UPDATE_DEFINITION_ID() {
        // Index: pk_process
    }

}
