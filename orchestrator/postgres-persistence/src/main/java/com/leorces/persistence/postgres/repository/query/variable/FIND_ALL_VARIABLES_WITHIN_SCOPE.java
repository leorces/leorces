package com.leorces.persistence.postgres.repository.query.variable;

public final class FIND_ALL_VARIABLES_WITHIN_SCOPE {

    public static final String FIND_ALL_VARIABLES_WITHIN_SCOPE_QUERY = """
            SELECT *
            FROM variable
            WHERE process_id = :processId
              AND execution_definition_id = ANY(:scope)
            """;

    private FIND_ALL_VARIABLES_WITHIN_SCOPE() {
    }

}
