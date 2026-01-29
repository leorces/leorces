package com.leorces.persistence.postgres.repository.query.process;

public class INCIDENT {

    public static final String INCIDENT_QUERY = """
            UPDATE process
            SET process_state      = 'INCIDENT',
                process_updated_at = NOW()
            WHERE process_id = :processId;
            """;

    private INCIDENT() {
        // Index: pk_process
    }

}
