--liquibase formatted sql

--changeset leorces:1
-- Create the process_definition table
CREATE TABLE IF NOT EXISTS definition
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

    -- Primary key constraint
    CONSTRAINT pk_definition PRIMARY KEY (definition_id),

    -- Unique constraint on definition_key + definition_version
    CONSTRAINT uq_definition_key_version UNIQUE (definition_key, definition_version)
);

-- Create the process table
CREATE TABLE IF NOT EXISTS process
(
    process_id             TEXT      NOT NULL,
    root_process_id        TEXT,
    process_parent_id      TEXT,
    process_definition_id  TEXT      NOT NULL,
    process_definition_key TEXT      NOT NULL,
    process_business_key   TEXT,
    process_state          TEXT      NOT NULL,
    process_created_at     TIMESTAMP NOT NULL,
    process_updated_at     TIMESTAMP NOT NULL,
    process_started_at     TIMESTAMP NOT NULL,
    process_completed_at   TIMESTAMP,

    -- Primary key constraint
    CONSTRAINT pk_process PRIMARY KEY (process_id)
);

-- Create the activity table
CREATE TABLE IF NOT EXISTS activity
(
    activity_id                   TEXT      NOT NULL,
    activity_definition_id        TEXT      NOT NULL,
    activity_parent_definition_id TEXT,
    activity_type                 TEXT      NOT NULL,
    activity_state                TEXT      NOT NULL,
    activity_created_at           TIMESTAMP NOT NULL,
    activity_updated_at           TIMESTAMP NOT NULL,
    activity_started_at           TIMESTAMP,
    activity_completed_at         TIMESTAMP,
    activity_retries              INTEGER   NOT NULL DEFAULT 0,
    activity_timeout        TIMESTAMP,
    activity_failure_reason TEXT,
    activity_failure_trace  TEXT,
    activity_async                BOOLEAN   NOT NULL,
    process_id                    TEXT      NOT NULL,
    process_definition_id         TEXT      NOT NULL,
    process_definition_key        TEXT      NOT NULL,

    -- Primary key constraint
    CONSTRAINT pk_activity PRIMARY KEY (activity_id)
);

CREATE INDEX IF NOT EXISTS idx_activity_timeout
    ON activity (activity_timeout)
    WHERE activity_timeout IS NOT NULL;

-- Create the activity_queue table
CREATE TABLE IF NOT EXISTS activity_queue
(
    activity_queue_topic      TEXT      NOT NULL,
    activity_id               TEXT      NOT NULL,
    process_definition_key    TEXT      NOT NULL,
    activity_queue_created_at TIMESTAMP NOT NULL,
    activity_queue_updated_at TIMESTAMP NOT NULL,

    -- Primary key constraint
    CONSTRAINT pk_activity_queue PRIMARY KEY (activity_id)
);

-- Create the execution_variable table
CREATE TABLE IF NOT EXISTS variable
(
    variable_id             TEXT      NOT NULL,
    variable_key            TEXT      NOT NULL,
    variable_value          TEXT      NULL,
    variable_type           TEXT      NULL,
    variable_created_at     TIMESTAMP NOT NULL,
    variable_updated_at     TIMESTAMP NOT NULL,
    process_id              TEXT      NOT NULL,
    execution_id            TEXT      NOT NULL, -- Process or activity
    execution_definition_id TEXT      NOT NULL,

    -- Primary key constraint
    CONSTRAINT pk_variable PRIMARY KEY (variable_id)
);

CREATE INDEX IF NOT EXISTS idx_variable_process_id
    ON variable (process_id);

-- Create the history table
CREATE TABLE IF NOT EXISTS history
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

    -- Primary key constraint
    CONSTRAINT pk_history_item PRIMARY KEY (process_id)
);

-- Create the shedlock table
CREATE TABLE shedlock
(
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP    NOT NULL,
    locked_at  TIMESTAMP    NOT NULL,
    locked_by  VARCHAR(255) NOT NULL,

    -- Primary key constraint
    CONSTRAINT pk_name PRIMARY KEY (name)
);