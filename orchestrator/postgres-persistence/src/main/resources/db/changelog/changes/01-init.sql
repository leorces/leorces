--liquibase formatted sql

--changeset leorces:1

CREATE EXTENSION IF NOT EXISTS pg_trgm;

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

CREATE INDEX idx_definition_created
    ON definition (definition_created_at DESC);

-- Trigram index for fast search by ID, Key or Name
CREATE INDEX idx_definition_search_trgm
    ON definition USING GIN ((definition_id::TEXT || ' ' || definition_key || ' ' || definition_name) gin_trgm_ops);

-- ============================
-- Table: definition_suspended
-- ============================
CREATE TABLE definition_suspended
(
    definition_id        TEXT    NOT NULL,
    definition_suspended BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_definition_suspended PRIMARY KEY (definition_id)
);

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

CREATE INDEX idx_process_parent_id
    ON process (process_parent_id)
    WHERE process_parent_id IS NOT NULL;

CREATE INDEX idx_process_created_at
    ON process (process_created_at DESC);

CREATE INDEX idx_process_completed_at
    ON process (process_completed_at ASC)
    WHERE process_completed_at IS NOT NULL;

CREATE INDEX idx_process_definition_id
    ON process (process_definition_id);

CREATE INDEX idx_process_definition_key
    ON process (process_definition_key);

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
    activity_topic          TEXT,
    activity_created_at           TIMESTAMP NOT NULL,
    activity_updated_at           TIMESTAMP NOT NULL,
    activity_started_at           TIMESTAMP,
    activity_completed_at         TIMESTAMP,
    activity_retries              INTEGER   NOT NULL DEFAULT 0,
    activity_timeout        TIMESTAMP,
    activity_failure_reason TEXT,
    activity_failure_trace  TEXT,
    activity_async          BOOLEAN NOT NULL DEFAULT FALSE,
    process_id                    TEXT      NOT NULL,
    process_definition_id         TEXT      NOT NULL,
    process_definition_key        TEXT      NOT NULL,

    CONSTRAINT pk_activity PRIMARY KEY (activity_id)
);

CREATE INDEX idx_activity_scheduled
    ON activity (activity_topic, process_definition_key, activity_created_at, process_id)
    WHERE activity_state = 'SCHEDULED';

CREATE INDEX idx_activity_timeout_active
    ON activity (activity_timeout)
    WHERE activity_state IN ('ACTIVE', 'SCHEDULED') AND activity_timeout IS NOT NULL;

CREATE INDEX idx_activity_process_lookup
    ON activity (process_id, activity_state, activity_completed_at, activity_definition_id);

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

CREATE INDEX idx_variable_execution_lookup
    ON variable (execution_id, variable_key, variable_value);

CREATE INDEX idx_variable_process_def
    ON variable (process_id, execution_definition_id);

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

CREATE INDEX idx_history_created_at
    ON history (process_created_at DESC);

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
