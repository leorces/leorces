package com.leorces.persistence.postgres.repository.query.definition;

public final class FIND_BY_KEY_AND_VERSION {

    public static final String FIND_BY_KEY_AND_VERSION_QUERY = """
            SELECT *
            FROM definition
            WHERE definition_key = :definitionKey
              AND definition_version = :version;
            """;

    private FIND_BY_KEY_AND_VERSION() {
        // Index: uq_definition_key_version
    }

}
