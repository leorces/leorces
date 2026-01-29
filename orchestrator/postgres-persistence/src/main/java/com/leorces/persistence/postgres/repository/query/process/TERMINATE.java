package com.leorces.persistence.postgres.repository.query.process;

public class TERMINATE {

    public static final String TERMINATE_QUERY = """
            UPDATE process
            SET process_state        = 'TERMINATED',
                process_suspended    = false,
                process_updated_at   = NOW(),
                process_completed_at = NOW()
            WHERE process_id = :processId;
            """;

    private TERMINATE() {
        // Index: pk_process
    }

}
