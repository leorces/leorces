package com.leorces.persistence.postgres.repository.query.activity;

public class CHANGE_STATE {

    public static final String CHANGE_STATE_QUERY = """
            UPDATE activity
            SET activity_state      = :state,
                activity_updated_at = NOW()
            WHERE activity_id = :activityId;
            """;

    private CHANGE_STATE() {
    }

}
