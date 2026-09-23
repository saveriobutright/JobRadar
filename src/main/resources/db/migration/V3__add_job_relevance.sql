ALTER TABLE job_postings
    ADD COLUMN relevance_score SMALLINT NOT NULL DEFAULT 0,
    ADD COLUMN role_points SMALLINT NOT NULL DEFAULT 0,
    ADD COLUMN skill_points SMALLINT NOT NULL DEFAULT 0,
    ADD COLUMN job_type_points SMALLINT NOT NULL DEFAULT 0,
    ADD COLUMN remote_points SMALLINT NOT NULL DEFAULT 0,
    ADD COLUMN matched_roles TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    ADD COLUMN matched_skills TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    ADD COLUMN matched_job_types TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    ADD COLUMN scored_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE job_postings
    ADD CONSTRAINT ck_job_postings_relevance_range
        CHECK (
            relevance_score BETWEEN 0 AND 100
            ),
    ADD CONSTRAINT ck_job_postings_relevance_sum
        CHECK (
            relevance_score = role_points
                + skill_points
                + job_type_points
                + remote_points
        );

CREATE INDEX idx_job_postings_relevance
    ON job_postings (
                     relevance_score DESC,
                     posted_at DESC,
                     id DESC
        );
