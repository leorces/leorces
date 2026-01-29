package com.leorces.persistence.postgres.repository.query.variable;

public final class FIND_ALL_PROCESS_VARIABLES {

    public static final String FIND_ALL_PROCESS_VARIABLES_QUERY = """
            SELECT *
            FROM variable
            WHERE execution_id = :processId
              AND process_id = :processId
            """;

    private FIND_ALL_PROCESS_VARIABLES() {
    }

}
