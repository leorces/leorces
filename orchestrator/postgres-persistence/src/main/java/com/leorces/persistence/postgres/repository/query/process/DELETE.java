package com.leorces.persistence.postgres.repository.query.process;

public class DELETE {

    public static final String DELETE_QUERY = """
            DELETE FROM process
            WHERE process_id = :processId;
            """;

    private DELETE() {
        // Index: pk_process
    }

}
