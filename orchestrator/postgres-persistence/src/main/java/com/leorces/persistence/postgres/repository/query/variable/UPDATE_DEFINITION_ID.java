package com.leorces.persistence.postgres.repository.query.variable;

public class UPDATE_DEFINITION_ID {

    public static final String UPDATE_DEFINITION_ID_QUERY = """
            UPDATE variable
            SET execution_definition_id = :toDefinitionId,
                variable_updated_at      = NOW()
            WHERE process_id = ANY (:processIds);
            """;

    private UPDATE_DEFINITION_ID() {
    }

}
