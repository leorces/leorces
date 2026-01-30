package com.leorces.persistence.postgres.repository.query.definition;

public final class FIND_LATEST_BY_KEY {

    public static final String FIND_LATEST_BY_KEY_QUERY = """
            SELECT *
            FROM definition
            WHERE definition_key = :definitionKey
            ORDER BY definition_key, definition_version DESC
            LIMIT 1;
            """;

    private FIND_LATEST_BY_KEY() {
        // Index: uq_definition_key_version
    }

}
