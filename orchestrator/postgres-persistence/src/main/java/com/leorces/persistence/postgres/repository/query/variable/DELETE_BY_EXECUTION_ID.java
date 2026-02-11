package com.leorces.persistence.postgres.repository.query.variable;

public class DELETE_BY_EXECUTION_ID {

    public static final String DELETE_BY_PROCESS_ID_QUERY = """
            DELETE FROM variable
            WHERE execution_id = :executionId;
            """;

    private DELETE_BY_EXECUTION_ID() {
    }

}
