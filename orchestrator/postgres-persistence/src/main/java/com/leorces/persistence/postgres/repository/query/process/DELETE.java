package com.leorces.persistence.postgres.repository.query.process;

public class DELETE {

    public static final String DELETE_QUERY = """
            UPDATE process
            SET process_state        = 'DELETED',
                process_suspended    = false,
                process_updated_at   = NOW(),
                process_completed_at = NOW()
            WHERE process_id = :processId;
            """;

    private DELETE() {
    }

}
