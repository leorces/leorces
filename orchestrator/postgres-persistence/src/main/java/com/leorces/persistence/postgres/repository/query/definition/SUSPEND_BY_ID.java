package com.leorces.persistence.postgres.repository.query.definition;

public final class SUSPEND_BY_ID {

    public static final String SUSPEND_BY_ID_QUERY = """
            UPDATE definition
            SET definition_suspended = TRUE
            WHERE definition_id = :definitionId;
            """;

    private SUSPEND_BY_ID() {
        // Index: pk_definition
    }

}
