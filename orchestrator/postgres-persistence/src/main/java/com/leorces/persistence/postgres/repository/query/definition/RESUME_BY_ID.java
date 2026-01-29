package com.leorces.persistence.postgres.repository.query.definition;

public final class RESUME_BY_ID {

    public static final String RESUME_BY_ID_QUERY = """
            INSERT INTO definition_suspended (definition_id, definition_suspended)
            VALUES (:definitionId, FALSE)
            ON CONFLICT (definition_id)
                DO UPDATE
                SET definition_suspended = FALSE;
            """;

    private RESUME_BY_ID() {
        // Index: pk_definition_suspended
    }

}
