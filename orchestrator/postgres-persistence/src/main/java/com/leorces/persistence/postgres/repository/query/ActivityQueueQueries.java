package com.leorces.persistence.postgres.repository.query;

public final class ActivityQueueQueries {

    public static final String FIND_ALL = """
            SELECT activity_id
            FROM activity_queue
            WHERE activity_queue_topic = :topic
              AND process_definition_key = :processDefinitionKey
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """;

    private ActivityQueueQueries() {
        // Utility class
    }

}