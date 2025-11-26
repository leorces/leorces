package com.leorces.persistence.postgres.repository.query;

public final class VariableQueries {

    public static final String FIND_ALL_VARIABLES_WITHIN_SCOPE = """
            SELECT *
            FROM variable
            WHERE process_id = :processId
              AND execution_definition_id IN (:scope)
            """;

    public static final String FIND_ALL_PROCESS_VARIABLES = """
            SELECT *
            FROM variable
            WHERE execution_id = :processId
              AND process_id = :processId
            """;

    private VariableQueries() {
        // Utility class
    }

}