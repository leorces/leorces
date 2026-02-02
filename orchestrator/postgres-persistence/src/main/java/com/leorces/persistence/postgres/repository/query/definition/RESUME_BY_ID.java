package com.leorces.persistence.postgres.repository.query.definition;

public final class RESUME_BY_ID {

    public static final String RESUME_BY_ID_QUERY = """
            UPDATE definition
            SET definition_suspended = FALSE
            WHERE definition_id = :definitionId;
            """;

    private RESUME_BY_ID() {
        // Index: pk_definition
    }

}
