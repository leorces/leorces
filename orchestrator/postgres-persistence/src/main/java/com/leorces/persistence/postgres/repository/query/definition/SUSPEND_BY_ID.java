package com.leorces.persistence.postgres.repository.query.definition;

public final class SUSPEND_BY_ID {

    public static final String SUSPEND_BY_ID_QUERY = """
            INSERT INTO definition_suspended (definition_id, definition_suspended)
            VALUES (:definitionId, TRUE)
            ON CONFLICT (definition_id)
                DO UPDATE
                SET definition_suspended = TRUE;
            """;

    private SUSPEND_BY_ID() {
        // Index: pk_definition_suspended
    }

}
