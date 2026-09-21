CREATE TABLE job_postings
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source        VARCHAR(100) NOT NULL,
    source_id     VARCHAR(255) NOT NULL,
    title         TEXT         NOT NULL,
    company       TEXT         NOT NULL,
    location      TEXT,
    remote        BOOLEAN      NOT NULL,
    source_url    TEXT         NOT NULL,
    posted_at     TIMESTAMPTZ  NOT NULL,
    first_seen_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_job_postings_source_source_id
        UNIQUE (source, source_id)
);

CREATE INDEX idx_job_postings_posted_at
    ON job_postings (posted_at DESC);