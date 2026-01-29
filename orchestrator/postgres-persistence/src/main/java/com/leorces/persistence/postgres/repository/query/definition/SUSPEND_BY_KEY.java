package com.leorces.persistence.postgres.repository.query.definition;

public final class SUSPEND_BY_KEY {

    public static final String SUSPEND_BY_KEY_QUERY = """
            INSERT INTO definition_suspended (definition_id, definition_suspended)
            SELECT d.definition_id, TRUE
            FROM definition d
            WHERE d.definition_key = :definitionKey
            ON CONFLICT (definition_id)
                DO UPDATE
                SET definition_suspended = TRUE;
            """;

    private SUSPEND_BY_KEY() {
        // Index: uq_definition_key_version, pk_definition_suspended
    }

}
