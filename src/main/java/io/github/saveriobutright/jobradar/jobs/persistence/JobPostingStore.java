package io.github.saveriobutright.jobradar.jobs.persistence;

import io.github.saveriobutright.jobradar.jobs.JobPosting;
import io.github.saveriobutright.jobradar.jobs.JobSearchCriteria;
import io.github.saveriobutright.jobradar.jobs.StoredJobPosting;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private static final String FIND_SQL = """
            SELECT
                id,
                source,
                source_id,
                title,
                company,
                location,
                remote,
                source_url,
                posted_at,
                first_seen_at,
                last_seen_at
            FROM job_postings
            %s
            ORDER BY posted_at DESC, id DESC
            LIMIT :limit
            OFFSET :offset
            """;

    private static final String COUNT_SQL = """
            SELECT COUNT(*)
            FROM job_postings
            %s
            """;

    private static final RowMapper<StoredJobPosting> STORED_JOB_ROW_MAPPER =
            (resultSet, rowNumber) -> new StoredJobPosting(
                    resultSet.getLong("id"),
                    resultSet.getString("source"),
                    resultSet.getString("source_id"),
                    resultSet.getString("title"),
                    resultSet.getString("company"),
                    resultSet.getString("location"),
                    resultSet.getBoolean("remote"),
                    URI.create(resultSet.getString("source_url")),
                    resultSet.getObject(
                            "posted_at",
                            OffsetDateTime.class
                    ).toInstant(),
                    resultSet.getObject(
                            "first_seen_at",
                            OffsetDateTime.class
                    ).toInstant(),
                    resultSet.getObject(
                            "last_seen_at",
                            OffsetDateTime.class
                    ).toInstant()
            );

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

    public List<StoredJobPosting> find(
            JobSearchCriteria criteria
    ) {
        SearchSql searchSql = buildSearchSql(criteria);

        return jdbcClient
                .sql(FIND_SQL.formatted(
                        searchSql.whereClause()
                ))
                .params(searchSql.parameters())
                .param("limit", criteria.size())
                .param("offset", criteria.offset())
                .query(STORED_JOB_ROW_MAPPER)
                .list();
    }

    public long count(JobSearchCriteria criteria) {
        SearchSql searchSql = buildSearchSql(criteria);

        return jdbcClient
                .sql(COUNT_SQL.formatted(
                        searchSql.whereClause()
                ))
                .params(searchSql.parameters())
                .query(Long.class)
                .single();
    }

    private static SearchSql buildSearchSql(
            JobSearchCriteria criteria
    ) {
        Objects.requireNonNull(
                criteria,
                "criteria must not be null"
        );

        List<String> conditions = new ArrayList<>();
        Map<String, Object> parameters = new LinkedHashMap<>();

        if (criteria.query() != null) {
            conditions.add("""
                    (
                        POSITION(LOWER(:query) IN LOWER(title)) > 0
                        OR POSITION(LOWER(:query) IN LOWER(company)) > 0
                    )
                    """.strip());
            parameters.put("query", criteria.query());
        }

        if (criteria.location() != null) {
            conditions.add("""
                    POSITION(
                        LOWER(:location) IN LOWER(location)
                    ) > 0
                    """.strip());
            parameters.put("location", criteria.location());
        }

        if (criteria.remote() != null) {
            conditions.add("remote = :remote");
            parameters.put("remote", criteria.remote());
        }

        String whereClause = conditions.isEmpty()
                ? ""
                : "WHERE " + String.join(
                        "\nAND ",
                        conditions
                );

        return new SearchSql(
                whereClause,
                Map.copyOf(parameters)
        );
    }

    private record SearchSql(
            String whereClause,
            Map<String, Object> parameters
    ) {
    }
}
