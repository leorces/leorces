package com.leorces.persistence.postgres.repository.query.definition;

public class FIND_FULL_BY_ID {

    public static final String FIND_FULL_BY_ID_QUERY = """
            SELECT *
            FROM definition
            WHERE definition_id = :definitionId;
            """;

    private FIND_FULL_BY_ID() {
    }

}
