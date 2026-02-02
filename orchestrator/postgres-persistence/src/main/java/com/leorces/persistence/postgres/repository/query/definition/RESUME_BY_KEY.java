package com.leorces.persistence.postgres.repository.query.definition;

public final class RESUME_BY_KEY {

    public static final String RESUME_BY_KEY_QUERY = """
            UPDATE definition
            SET definition_suspended = FALSE
            WHERE definition_key = :definitionKey;
            """;

    private RESUME_BY_KEY() {
        // Index: uq_definition_key_version
    }

}
