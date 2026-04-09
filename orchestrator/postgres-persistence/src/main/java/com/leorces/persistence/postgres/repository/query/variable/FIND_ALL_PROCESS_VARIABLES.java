package com.leorces.persistence.postgres.repository.query.variable;

public final class FIND_ALL_PROCESS_VARIABLES {

    public static final String FIND_ALL_PROCESS_VARIABLES_QUERY = """
            SELECT *
            FROM variable
            WHERE process_id = :processId
            """;

    private FIND_ALL_PROCESS_VARIABLES() {
    }

}
