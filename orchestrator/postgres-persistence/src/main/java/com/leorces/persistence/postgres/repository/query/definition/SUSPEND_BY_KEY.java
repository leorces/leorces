package com.leorces.persistence.postgres.repository.query.definition;

public final class SUSPEND_BY_KEY {

    public static final String SUSPEND_BY_KEY_QUERY = """
            UPDATE definition
            SET definition_suspended = TRUE
            WHERE definition_key = :definitionKey;
            """;

    private SUSPEND_BY_KEY() {
        // Index: uq_definition_key_version
    }

}
