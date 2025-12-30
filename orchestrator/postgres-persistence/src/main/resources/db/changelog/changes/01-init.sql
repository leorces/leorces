--liquibase formatted sql

--changeset leorces:1

-- ============================
-- Table: definition
-- ============================
CREATE TABLE definition
(
    definition_id         TEXT      NOT NULL,
    definition_key        TEXT      NOT NULL,
    definition_name       TEXT      NOT NULL,
    definition_version    INTEGER   NOT NULL DEFAULT 0,
    definition_schema     TEXT      NOT NULL,
    definition_origin     TEXT      NOT NULL,
    definition_deployment TEXT      NOT NULL,
    definition_created_at TIMESTAMP NOT NULL,
    definition_updated_at TIMESTAMP NOT NULL,
    definition_data       JSONB     NOT NULL,

    CONSTRAINT pk_definition PRIMARY KEY (definition_id),
    CONSTRAINT uq_definition_key_version UNIQUE (definition_key, definition_version)
);

CREATE INDEX idx_definition_created_at_desc
    ON definition (definition_created_at DESC);

CREATE INDEX idx_definition_search_lower
    ON definition (LOWER(definition_id), LOWER(definition_key), LOWER(definition_name));

-- ============================
-- Table: process
-- ============================
CREATE TABLE process
(
    process_id             TEXT      NOT NULL,
    root_process_id        TEXT,
    process_parent_id      TEXT,
    process_definition_id  TEXT      NOT NULL,
    process_definition_key TEXT      NOT NULL,
    process_business_key TEXT    NOT NULL,
    process_state          TEXT      NOT NULL,
    process_suspended    BOOLEAN NOT NULL DEFAULT FALSE,
    process_created_at     TIMESTAMP NOT NULL,
    process_updated_at     TIMESTAMP NOT NULL,
    process_started_at     TIMESTAMP NOT NULL,
    process_completed_at   TIMESTAMP,

    CONSTRAINT pk_process PRIMARY KEY (process_id)
);

CREATE INDEX idx_process_state_defkey_created
    ON process (process_state, process_definition_key, process_created_at DESC);

CREATE INDEX idx_process_business_defkey
    ON process (process_business_key, process_definition_key);

CREATE INDEX idx_process_defid_suspended_completed
    ON process (process_definition_id)
    WHERE process_suspended = false AND process_completed_at IS NULL;

CREATE INDEX idx_process_defkey_suspended_completed
    ON process (process_definition_key)
    WHERE process_suspended = false AND process_completed_at IS NULL;

CREATE INDEX idx_process_parent
    ON process (process_parent_id);

-- ============================
-- Table: activity
-- ============================
CREATE TABLE activity
(
    activity_id                   TEXT      NOT NULL,
    activity_definition_id        TEXT      NOT NULL,
    activity_parent_definition_id TEXT,
    activity_type                 TEXT      NOT NULL,
    activity_state                TEXT      NOT NULL,
    activity_topic TEXT,
    activity_created_at           TIMESTAMP NOT NULL,
    activity_updated_at           TIMESTAMP NOT NULL,
    activity_started_at           TIMESTAMP,
    activity_completed_at         TIMESTAMP,
    activity_retries              INTEGER   NOT NULL DEFAULT 0,
    activity_timeout        TIMESTAMP,
    activity_failure_reason TEXT,
    activity_failure_trace  TEXT,
    activity_async BOOLEAN NOT NULL DEFAULT false,
    process_id                    TEXT      NOT NULL,
    process_definition_id         TEXT      NOT NULL,
    process_definition_key        TEXT      NOT NULL,

    CONSTRAINT pk_activity PRIMARY KEY (activity_id)
);

CREATE INDEX idx_activity_process_state_completed
    ON activity (process_id, activity_state, activity_completed_at);

CREATE INDEX idx_activity_topic_scheduled
    ON activity (activity_topic, process_definition_key, activity_created_at)
    WHERE activity_state = 'SCHEDULED';

CREATE INDEX idx_activity_timeout_active_scheduled
    ON activity (activity_timeout)
    WHERE activity_state IN ('ACTIVE', 'SCHEDULED') AND activity_timeout IS NOT NULL;

CREATE INDEX idx_activity_process_def_state_completed
    ON activity (process_id, activity_definition_id, activity_state, activity_completed_at)
    WHERE activity_state IN ('ACTIVE', 'SCHEDULED');

CREATE INDEX idx_activity_defid_created
    ON activity (activity_definition_id, activity_created_at);

-- ============================
-- Table: variable
-- ============================
CREATE TABLE variable
(
    variable_id             TEXT      NOT NULL,
    variable_key            TEXT      NOT NULL,
    variable_value TEXT,
    variable_type  TEXT,
    variable_created_at     TIMESTAMP NOT NULL,
    variable_updated_at     TIMESTAMP NOT NULL,
    process_id              TEXT      NOT NULL,
    execution_id   TEXT NOT NULL, -- Process id or activity id (polymorphic, no FK)
    execution_definition_id TEXT      NOT NULL,

    CONSTRAINT pk_variable PRIMARY KEY (variable_id)
);

CREATE INDEX idx_variable_execution
    ON variable (execution_id);

CREATE INDEX idx_variable_process_def
    ON variable (process_id, execution_definition_id);

CREATE INDEX idx_variable_key_value_execution
    ON variable (execution_id, variable_key, variable_value);

-- ============================
-- Table: history
-- ============================
CREATE TABLE history
(
    process_id           TEXT      NOT NULL,
    root_process_id      TEXT,
    process_parent_id    TEXT,
    process_business_key TEXT,
    history_data         BYTEA,
    process_created_at   TIMESTAMP NOT NULL,
    process_updated_at   TIMESTAMP,
    process_started_at   TIMESTAMP,
    process_completed_at TIMESTAMP,

    CONSTRAINT pk_history_item PRIMARY KEY (process_id)
);

-- ============================
-- Table: shedlock
-- ============================
CREATE TABLE shedlock
(
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP    NOT NULL,
    locked_at  TIMESTAMP    NOT NULL,
    locked_by  VARCHAR(255) NOT NULL,

    CONSTRAINT pk_name PRIMARY KEY (name)
);

-- End of changeset

