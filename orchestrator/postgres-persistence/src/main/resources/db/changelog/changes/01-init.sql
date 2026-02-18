--liquibase formatted sql

--changeset leorces:1

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;

-- ============================
-- Table: definition
-- ============================
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
    definition_suspended  BOOLEAN   NOT NULL DEFAULT FALSE,
    definition_data       JSONB     NOT NULL,

    CONSTRAINT pk_definition PRIMARY KEY (definition_id),
    CONSTRAINT uq_definition_key_version UNIQUE (definition_key, definition_version)
);

CREATE INDEX IF NOT EXISTS idx_definition_created
    ON definition (definition_created_at DESC);

CREATE INDEX IF NOT EXISTS idx_definition_search_trgm
    ON definition
        USING GIN ((definition_id || ' ' || definition_key || ' ' || definition_name) public.gin_trgm_ops);

-- ============================
-- Table: process
-- ============================
CREATE TABLE IF NOT EXISTS process
(
    process_id             TEXT      NOT NULL,
    root_process_id        TEXT,
    process_parent_id      TEXT,
    process_definition_id  TEXT      NOT NULL,
    process_definition_key TEXT      NOT NULL,
    process_business_key   TEXT      NOT NULL,
    process_state          TEXT      NOT NULL,
    process_suspended      BOOLEAN   NOT NULL DEFAULT FALSE,
    process_created_at     TIMESTAMP NOT NULL,
    process_updated_at     TIMESTAMP NOT NULL,
    process_started_at     TIMESTAMP NOT NULL,
    process_completed_at   TIMESTAMP,

    CONSTRAINT pk_process PRIMARY KEY (process_id)
);

CREATE INDEX IF NOT EXISTS idx_process_definition_id
    ON process (process_definition_id);

CREATE INDEX IF NOT EXISTS idx_process_definition_key
    ON process (process_definition_key);

CREATE INDEX IF NOT EXISTS idx_process_state
    ON process (process_state);

CREATE INDEX IF NOT EXISTS idx_process_created_at
    ON process (process_created_at);

CREATE INDEX IF NOT EXISTS idx_process_completed_at
    ON process (process_completed_at);

CREATE INDEX IF NOT EXISTS idx_process_business_key
    ON process (process_business_key);

CREATE INDEX IF NOT EXISTS idx_process_eligible
    ON process (process_state, process_completed_at)
    WHERE process_state IN ('COMPLETED', 'TERMINATED', 'DELETED');

CREATE INDEX IF NOT EXISTS idx_process_parent_id
    ON process (process_parent_id);

-- ============================
-- Table: activity
-- ============================
CREATE TABLE IF NOT EXISTS activity
(
    activity_id                   TEXT      NOT NULL,
    activity_definition_id        TEXT      NOT NULL,
    activity_parent_definition_id TEXT,
    activity_type                 TEXT      NOT NULL,
    activity_state                TEXT      NOT NULL,
    activity_topic                TEXT,
    activity_created_at           TIMESTAMP NOT NULL,
    activity_updated_at           TIMESTAMP NOT NULL,
    activity_started_at           TIMESTAMP,
    activity_completed_at         TIMESTAMP,
    activity_retries              INTEGER   NOT NULL DEFAULT 0,
    activity_timeout              TIMESTAMP,
    activity_failure_reason       TEXT,
    activity_failure_trace        TEXT,
    activity_async                BOOLEAN   NOT NULL DEFAULT FALSE,
    process_id                    TEXT      NOT NULL,
    process_definition_key        TEXT      NOT NULL,

    CONSTRAINT pk_activity PRIMARY KEY (activity_id)
);

CREATE INDEX IF NOT EXISTS idx_activity_process_def_state
    ON activity (process_id, activity_definition_id, activity_state);

CREATE INDEX IF NOT EXISTS idx_activity_process_state
    ON activity (process_id, activity_state);

CREATE INDEX IF NOT EXISTS idx_activity_topic_process_key_state
    ON activity (activity_topic, process_definition_key, activity_created_at)
    WHERE activity_state = 'SCHEDULED';

CREATE INDEX IF NOT EXISTS idx_activity_timeout_state
    ON activity (activity_timeout)
    WHERE activity_state IN ('ACTIVE', 'SCHEDULED');

-- ============================
-- Table: variable
-- ============================
CREATE TABLE IF NOT EXISTS variable
(
    variable_id             TEXT      NOT NULL,
    variable_key            TEXT      NOT NULL,
    variable_value          TEXT,
    variable_type           TEXT,
    variable_created_at     TIMESTAMP NOT NULL,
    variable_updated_at     TIMESTAMP NOT NULL,
    process_id              TEXT      NOT NULL,
    execution_id            TEXT      NOT NULL,
    execution_definition_id TEXT      NOT NULL,

    CONSTRAINT pk_variable PRIMARY KEY (variable_id)
);

CREATE INDEX IF NOT EXISTS idx_variable_execution_lookup
    ON variable (execution_id, variable_key, variable_value);

CREATE INDEX IF NOT EXISTS idx_variable_execution_definition_id
    ON variable (execution_id, execution_definition_id);

CREATE INDEX IF NOT EXISTS idx_variable_process_scope
    ON variable (process_id, execution_definition_id);

-- ============================
-- Table: history
-- ============================
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

    CONSTRAINT pk_history_item PRIMARY KEY (process_id)
);

CREATE INDEX IF NOT EXISTS idx_history_created_at
    ON history (process_created_at DESC);

CREATE INDEX IF NOT EXISTS idx_history_business_key
    ON history (process_business_key);

-- ============================
-- Table: shedlock
-- ============================
CREATE TABLE IF NOT EXISTS shedlock
(
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP    NOT NULL,
    locked_at  TIMESTAMP    NOT NULL,
    locked_by  VARCHAR(255) NOT NULL,

    CONSTRAINT pk_name PRIMARY KEY (name)
);

-- ============================
-- Table: job
-- ============================
CREATE TABLE IF NOT EXISTS job
(
    job_id             TEXT      NOT NULL,
    job_type           TEXT      NOT NULL,
    job_state          TEXT      NOT NULL,
    job_input          JSONB,
    job_output         JSONB,
    job_retries        INTEGER   NOT NULL DEFAULT 0,
    job_failure_reason TEXT,
    job_failure_trace  TEXT,
    job_created_at     TIMESTAMP NOT NULL,
    job_updated_at     TIMESTAMP,
    job_started_at     TIMESTAMP,
    job_completed_at   TIMESTAMP,

    CONSTRAINT pk_job PRIMARY KEY (job_id)
);

CREATE INDEX IF NOT EXISTS idx_job_state
    ON job (job_state);

CREATE INDEX IF NOT EXISTS idx_job_created_at
    ON job (job_created_at DESC);

-- End of changeset
