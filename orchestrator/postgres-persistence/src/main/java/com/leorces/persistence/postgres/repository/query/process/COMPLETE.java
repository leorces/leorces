package com.leorces.persistence.postgres.repository.query.process;

public class COMPLETE {

    public static final String COMPLETE_QUERY = """
            UPDATE process
            SET process_state        = 'COMPLETED',
                process_suspended    = false,
                process_updated_at   = NOW(),
                process_completed_at = NOW()
            WHERE process_id = :processId;
            """;

    private COMPLETE() {
        // Index: pk_process
    }

}
