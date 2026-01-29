package com.leorces.persistence.postgres.repository.query.process;

public class CHANGE_STATE {

    public static final String CHANGE_STATE_QUERY = """
            UPDATE process
            SET process_state      = :state,
                process_updated_at = NOW()
            WHERE process_id = :processId;
            """;

    private CHANGE_STATE() {
        // Index: pk_process
    }

}
