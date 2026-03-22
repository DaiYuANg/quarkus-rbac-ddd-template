CREATE TABLE IF NOT EXISTS job_execution_history
(
    id                  BIGINT                      NOT NULL,
    job_name            TEXT                        NOT NULL,
    job_group           TEXT                        NOT NULL,
    job_class           TEXT,
    job_description     TEXT,
    trigger_name        TEXT,
    trigger_group       TEXT,
    schedule_time       TIMESTAMP WITH TIME ZONE,
    actual_fire_time    TIMESTAMP WITH TIME ZONE,
    previous_fire_time  TIMESTAMP WITH TIME ZONE,
    next_fire_time      TIMESTAMP WITH TIME ZONE,
    misfire_instruction INTEGER,
    start_time          TIMESTAMP WITH TIME ZONE   NOT NULL,
    end_time            TIMESTAMP WITH TIME ZONE,
    duration_ms         BIGINT,
    application_name    VARCHAR(255),
    node_id             VARCHAR(255),
    thread_name         VARCHAR(255),
    host_name           VARCHAR(255),
    status              SMALLINT,
    error_message       TEXT,
    error_type          TEXT,
    last_error_stack    TEXT,
    create_at           TIMESTAMP WITH TIME ZONE   NOT NULL DEFAULT now(),
    job_data_map        JSONB,
    trace_id            VARCHAR(255),
    PRIMARY KEY (id, start_time)
);

CREATE INDEX IF NOT EXISTS idx_job_execution_history_jobname ON job_execution_history (job_name);
CREATE INDEX IF NOT EXISTS idx_job_execution_history_jobgroup ON job_execution_history (job_group);
CREATE INDEX IF NOT EXISTS idx_job_execution_history_status ON job_execution_history (status);
CREATE INDEX IF NOT EXISTS idx_job_execution_history_appnode ON job_execution_history (application_name, node_id);
CREATE INDEX IF NOT EXISTS idx_job_execution_history_traceid ON job_execution_history (trace_id);
CREATE INDEX IF NOT EXISTS idx_job_execution_history_start_time ON job_execution_history (start_time DESC);
CREATE INDEX IF NOT EXISTS idx_job_execution_history_bizkey ON job_execution_history USING GIN (job_data_map jsonb_path_ops);
