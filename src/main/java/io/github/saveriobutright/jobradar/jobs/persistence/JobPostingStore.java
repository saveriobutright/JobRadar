package io.github.saveriobutright.jobradar.jobs.persistence;

import io.github.saveriobutright.jobradar.jobs.JobPosting;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Repository
public class JobPostingStore {

    private static final String UPSERT_SQL = """
            INSERT INTO job_postings (
                source,
                source_id,
                title,
                company,
                location,
                remote,
                source_url,
                posted_at
            )
            VALUES (
                :source,
                :sourceId,
                :title,
                :company,
                :location,
                :remote,
                :sourceUrl,
                :postedAt
            )
            ON CONFLICT (source, source_id)
            DO UPDATE SET
                title = EXCLUDED.title,
                company = EXCLUDED.company,
                location = EXCLUDED.location,
                remote = EXCLUDED.remote,
                source_url = EXCLUDED.source_url,
                posted_at = EXCLUDED.posted_at,
                last_seen_at = CURRENT_TIMESTAMP
            """;

    private final JdbcClient jdbcClient;

    public JobPostingStore(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Transactional
    public int upsertAll(List<JobPosting> jobs) {
        Objects.requireNonNull(jobs, "jobs must not be null");

        return jobs.stream()
                .mapToInt(this::upsert)
                .sum();
    }

    public int upsert(JobPosting job) {
        Objects.requireNonNull(job, "job must not be null");

        return jdbcClient.sql(UPSERT_SQL)
                .param("source", job.source())
                .param("sourceId", job.sourceId())
                .param("title", job.title())
                .param("company", job.company())
                .param("location", job.location(), Types.VARCHAR)
                .param("remote", job.remote())
                .param("sourceUrl", job.sourceUrl().toString())
                .param(
                        "postedAt",
                        job.postedAt().atOffset(ZoneOffset.UTC)
                )
                .update();
    }
}