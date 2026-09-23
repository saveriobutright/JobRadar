package io.github.saveriobutright.jobradar.jobs.persistence;

import io.github.saveriobutright.jobradar.jobs.JobPosting;
import io.github.saveriobutright.jobradar.jobs.JobSearchCriteria;
import io.github.saveriobutright.jobradar.jobs.JobSort;
import io.github.saveriobutright.jobradar.jobs.StoredJobPosting;
import io.github.saveriobutright.jobradar.jobs.scoring.JobRelevanceScore;
import io.github.saveriobutright.jobradar.jobs.scoring.ScoredJobPosting;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.SqlArrayValue;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Repository
public class JobPostingStore {

    private static final JobRelevanceScore UNSCORED_RELEVANCE =
            new JobRelevanceScore(
                    0,
                    0,
                    0,
                    0,
                    0,
                    List.of(),
                    List.of(),
                    List.of()
            );

    private static final String UPSERT_SQL = """
        INSERT INTO job_postings (
            source,
            source_id,
            title,
            company,
            description,
            tags,
            job_types,
            relevance_score,
            role_points,
            skill_points,
            job_type_points,
            remote_points,
            matched_roles,
            matched_skills,
            matched_job_types,
            scored_at,
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
            :description,
            :tags,
            :jobTypes,
            :relevanceScore,
            :rolePoints,
            :skillPoints,
            :jobTypePoints,
            :remotePoints,
            :matchedRoles,
            :matchedSkills,
            :matchedJobTypes,
            :scoredAt,
            :location,
            :remote,
            :sourceUrl,
            :postedAt
        )
        ON CONFLICT (source, source_id)
        DO UPDATE SET
            title = EXCLUDED.title,
            company = EXCLUDED.company,
            description = EXCLUDED.description,
            tags = EXCLUDED.tags,
            job_types = EXCLUDED.job_types,
            relevance_score = EXCLUDED.relevance_score,
            role_points = EXCLUDED.role_points,
            skill_points = EXCLUDED.skill_points,
            job_type_points = EXCLUDED.job_type_points,
            remote_points = EXCLUDED.remote_points,
            matched_roles = EXCLUDED.matched_roles,
            matched_skills = EXCLUDED.matched_skills,
            matched_job_types = EXCLUDED.matched_job_types,
            scored_at = EXCLUDED.scored_at,
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
                description,
                tags,
                job_types,
                relevance_score,
                role_points,
                skill_points,
                job_type_points,
                remote_points,
                matched_roles,
                matched_skills,
                matched_job_types,
                scored_at,
                location,
                remote,
                source_url,
                posted_at,
                first_seen_at,
                last_seen_at
            FROM job_postings
            %s
            ORDER BY %s
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
                    resultSet.getString("description"),
                    readTextArray(resultSet, "tags"),
                    readTextArray(resultSet, "job_types"),
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
                    ).toInstant(),
                    new JobRelevanceScore(
                            resultSet.getInt("relevance_score"),
                            resultSet.getInt("role_points"),
                            resultSet.getInt("skill_points"),
                            resultSet.getInt("job_type_points"),
                            resultSet.getInt("remote_points"),
                            readTextArray(
                                    resultSet,
                                    "matched_roles"
                            ),
                            readTextArray(
                                    resultSet,
                                    "matched_skills"
                            ),
                            readTextArray(
                                    resultSet,
                                    "matched_job_types"
                            )
                    ),
                    readNullableInstant(resultSet, "scored_at")
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

    @Transactional
    public int upsertAllScored(
            List<ScoredJobPosting> scoredJobs
    ) {
        Objects.requireNonNull(
                scoredJobs,
                "scoredJobs must not be null"
        );

        return scoredJobs.stream()
                .mapToInt(this::upsert)
                .sum();
    }

    public int upsert(JobPosting job) {
        return upsert(
                job,
                UNSCORED_RELEVANCE,
                null
        );
    }

    public int upsert(ScoredJobPosting scoredJob) {
        Objects.requireNonNull(
                scoredJob,
                "scoredJob must not be null"
        );

        return upsert(
                scoredJob.job(),
                scoredJob.relevance(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    private int upsert(
            JobPosting job,
            JobRelevanceScore relevance,
            OffsetDateTime scoredAt
    ) {
        Objects.requireNonNull(job, "job must not be null");
        Objects.requireNonNull(
                relevance,
                "relevance must not be null"
        );

        return jdbcClient.sql(UPSERT_SQL)
                .param("source", job.source())
                .param("sourceId", job.sourceId())
                .param("title", job.title())
                .param("company", job.company())
                .param("description", job.description())
                .param(
                        "tags",
                        new SqlArrayValue(
                                "text",
                                job.tags().toArray(String[]::new)
                        )
                )
                .param(
                        "jobTypes",
                        new SqlArrayValue(
                                "text",
                                job.jobTypes().toArray(String[]::new)
                        )
                )
                .param(
                        "relevanceScore",
                        relevance.score()
                )
                .param("rolePoints", relevance.rolePoints())
                .param("skillPoints", relevance.skillPoints())
                .param(
                        "jobTypePoints",
                        relevance.jobTypePoints()
                )
                .param(
                        "remotePoints",
                        relevance.remotePoints()
                )
                .param(
                        "matchedRoles",
                        new SqlArrayValue(
                                "text",
                                relevance.matchedRoles().toArray(String[]::new)
                        )
                )
                .param(
                        "matchedSkills",
                        new SqlArrayValue(
                                "text",
                                relevance.matchedSkills()
                                        .toArray(String[]::new)
                        )
                )
                .param(
                        "matchedJobTypes",
                        new SqlArrayValue(
                                "text",
                                relevance.matchedJobTypes()
                                        .toArray(String[]::new)
                        )
                )
                .param(
                        "scoredAt",
                        scoredAt,
                        Types.TIMESTAMP_WITH_TIMEZONE
                )
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
                        searchSql.whereClause(),
                        orderBy(criteria.sort())
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

    private static List<String> readTextArray(
            ResultSet resultSet,
            String columnName
    ) throws SQLException {
        Array sqlArray = resultSet.getArray(columnName);

        if (sqlArray == null) {
            return List.of();
        }

        try {
            String[] values = (String[]) sqlArray.getArray();
            return List.copyOf(Arrays.asList(values));
        } finally {
            sqlArray.free();
        }
    }

    private static java.time.Instant readNullableInstant(
            ResultSet resultSet,
            String columnName
    ) throws SQLException {
        OffsetDateTime value = resultSet.getObject(
                columnName,
                OffsetDateTime.class
        );

        return value == null
                ? null
                : value.toInstant();
    }

    private static String orderBy(JobSort sort) {
        return switch (sort) {
            case NEWEST ->
                    "posted_at DESC, id DESC";

            case RELEVANCE ->
                    """
                    relevance_score DESC,
                    posted_at DESC,
                    id DESC
                    """.strip();
        };
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
