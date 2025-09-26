package com.leorces.persistence.postgres.repository.query;

public final class VariableQueries {

    public static final String FIND_ALL = """
            SELECT *
            FROM variable
            WHERE process_id = :processId
              AND execution_definition_id IN (:scope)
            """;

    private VariableQueries() {
        // Utility class
    }

}