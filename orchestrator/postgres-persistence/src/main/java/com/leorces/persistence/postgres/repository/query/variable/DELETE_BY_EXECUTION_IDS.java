package com.leorces.persistence.postgres.repository.query.variable;

public class DELETE_BY_EXECUTION_IDS {

    public static final String DELETE_BY_PROCESS_IDS_QUERY = """
            DELETE FROM variable
            WHERE execution_id = ANY(:executionIds);
            """;

    private DELETE_BY_EXECUTION_IDS() {
    }

}
