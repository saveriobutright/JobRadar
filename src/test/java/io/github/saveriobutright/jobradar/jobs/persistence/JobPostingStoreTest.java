package io.github.saveriobutright.jobradar.jobs.persistence;

import io.github.saveriobutright.jobradar.TestcontainersConfiguration;
import io.github.saveriobutright.jobradar.jobs.JobPosting;
import io.github.saveriobutright.jobradar.jobs.JobSearchCriteria;
import io.github.saveriobutright.jobradar.jobs.StoredJobPosting;
import io.github.saveriobutright.jobradar.jobs.scoring.JobRelevanceScore;
import io.github.saveriobutright.jobradar.jobs.scoring.ScoredJobPosting;
import io.github.saveriobutright.jobradar.jobs.JobSort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class JobPostingStoreTest {

    @Autowired
    private JobPostingStore store;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void clearDatabase() {
        jdbcClient.sql("DELETE FROM job_postings").update();
    }

    @Test
    void updatesExistingJobWithoutCreatingDuplicate() {
        JobPosting original = new JobPosting(
                "Arbeitnow",
                "data-engineer-example",
                "Data Engineer",
                "Example Company",
                "Build initial data pipelines",
                List.of("Data"),
                List.of("Full-time"),
                "Berlin",
                false,
                URI.create("https://example.com/jobs/original"),
                Instant.parse("2026-09-20T08:00:00Z")
        );

        JobPosting updated = new JobPosting(
                "Arbeitnow",
                "data-engineer-example",
                "Senior Data Engineer",
                "Example Company",
                "Build scalable data platforms with Java.",
                List.of("Data", "Java"),
                List.of("Full-time", "Permanent"),
                "Remote",
                true,
                URI.create("https://example.com/jobs/updated"),
                Instant.parse("2026-09-21T08:00:00Z")
        );

        assertThat(store.upsert(original)).isEqualTo(1);
        assertThat(store.upsert(updated)).isEqualTo(1);

        Long count = jdbcClient.sql("""
                        SELECT COUNT(*)
                        FROM job_postings
                        WHERE source = :source
                          AND source_id = :sourceId
                        """)
                .param("source", "Arbeitnow")
                .param("sourceId", "data-engineer-example")
                .query(Long.class)
                .single();

        String title = jdbcClient.sql("""
                        SELECT title
                        FROM job_postings
                        WHERE source = :source
                          AND source_id = :sourceId
                        """)
                .param("source", "Arbeitnow")
                .param("sourceId", "data-engineer-example")
                .query(String.class)
                .single();

        Boolean remote = jdbcClient.sql("""
                        SELECT remote
                        FROM job_postings
                        WHERE source = :source
                          AND source_id = :sourceId
                        """)
                .param("source", "Arbeitnow")
                .param("sourceId", "data-engineer-example")
                .query(Boolean.class)
                .single();

        assertThat(count).isEqualTo(1);
        assertThat(title).isEqualTo("Senior Data Engineer");
        assertThat(remote).isTrue();

        JobSearchCriteria criteria = new JobSearchCriteria(
                1,
                10,
                null,
                null,
                null
        );

        StoredJobPosting storedJob = store.find(criteria).get(0);

        assertThat(storedJob.description())
                .isEqualTo(
                        "Build scalable data platforms with Java."
                );

        assertThat(storedJob.tags())
                .containsExactly("Data", "Java");

        assertThat(storedJob.jobTypes())
                .containsExactly("Full-time", "Permanent");
    }

    @Test
    void rollsBackPageWhenOneJobCannotBeStored() {
        JobPosting valid = new JobPosting(
                "Arbeitnow",
                "valid-job",
                "Data Engineer",
                "Example Company",
                "Berlin",
                false,
                URI.create("https://example.com/jobs/valid"),
                Instant.parse("2026-09-21T08:00:00Z")
        );

        JobPosting invalid = new JobPosting(
                "A".repeat(101),
                "invalid-job",
                "AI Engineer",
                "Example Company",
                "Remote",
                true,
                URI.create("https://example.com/jobs/invalid"),
                Instant.parse("2026-09-21T09:00:00Z")
        );

        assertThatThrownBy(() -> store.upsertAll(List.of(valid, invalid)))
                .isInstanceOf(DataIntegrityViolationException.class);

        Long count = jdbcClient.sql("""
                        SELECT COUNT(*)
                        FROM job_postings
                        """)
                .query(Long.class)
                .single();

        assertThat(count).isZero();
    }

    @Test
    void filtersOrdersAndPaginatesStoredJobs() {
        JobPosting newestMatch = new JobPosting(
                "Arbeitnow",
                "data-newest",
                "Data Engineer",
                "Example One",
                "Berlin",
                true,
                URI.create("https://example.com/jobs/data-newest"),
                Instant.parse("2026-09-22T10:00:00Z")
        );

        JobPosting olderMatch = new JobPosting(
                "Arbeitnow",
                "data-older",
                "Senior Data Engineer",
                "Example Two",
                "Berlin, Germany",
                true,
                URI.create("https://example.com/jobs/data-older"),
                Instant.parse("2026-09-21T10:00:00Z")
        );

        JobPosting excludedByRemoteFilter = new JobPosting(
                "Arbeitnow",
                "data-onsite",
                "Principal Data Engineer",
                "Example Three",
                "Berlin",
                false,
                URI.create("https://example.com/jobs/data-onsite"),
                Instant.parse("2026-09-23T10:00:00Z")
        );

        assertThat(store.upsertAll(List.of(
                newestMatch,
                olderMatch,
                excludedByRemoteFilter
        ))).isEqualTo(3);

        JobSearchCriteria criteria = new JobSearchCriteria(
                2,
                1,
                " DATA ",
                "ber",
                true
        );

        List<StoredJobPosting> jobs = store.find(criteria);
        long total = store.count(criteria);

        assertThat(total).isEqualTo(2);

        assertThat(jobs)
                .singleElement()
                .satisfies(storedJob -> {
                    assertThat(storedJob.id()).isPositive();
                    assertThat(storedJob.sourceId())
                            .isEqualTo("data-older");
                    assertThat(storedJob.title())
                            .isEqualTo("Senior Data Engineer");
                    assertThat(storedJob.location())
                            .isEqualTo("Berlin, Germany");
                    assertThat(storedJob.remote()).isTrue();
                    assertThat(storedJob.postedAt())
                            .isEqualTo(
                                    Instant.parse(
                                            "2026-09-21T10:00:00Z"
                                    )
                            );
                    assertThat(storedJob.firstSeenAt())
                            .isNotNull();
                    assertThat(storedJob.lastSeenAt())
                            .isNotNull();
                });

        JobSearchCriteria allCriteria = new JobSearchCriteria(
                1,
                10,
                null,
                null,
                null
        );

        assertThat(store.count(allCriteria)).isEqualTo(3);

        assertThat(store.find(allCriteria))
                .extracting(StoredJobPosting::sourceId)
                .containsExactly(
                        "data-onsite",
                        "data-newest",
                        "data-older"
                );
    }

    @Test
    void storesAndReadsExplainableRelevanceScore() {
        JobPosting job = new JobPosting(
                "Arbeitnow",
                "scored-data-engineer",
                "Senior Data Engineer",
                "Example Company",
                "Build streaming platforms with Java and SQL.",
                List.of("Kafka", "Data"),
                List.of("Full-time"),
                "Remote",
                true,
                URI.create(
                        "https://example.com/jobs/"
                                + "scored-data-engineer"
                ),
                Instant.parse("2026-09-23T10:00:00Z")
        );

        JobRelevanceScore relevance =
                new JobRelevanceScore(
                        90,
                        45,
                        30,
                        10,
                        5,
                        List.of("data engineer"),
                        List.of("java", "sql", "kafka"),
                        List.of("full-time")
                );

        ScoredJobPosting scoredJob =
                new ScoredJobPosting(
                        job,
                        relevance
                );

        assertThat(store.upsert(scoredJob))
                .isEqualTo(1);

        JobSearchCriteria criteria =
                new JobSearchCriteria(
                        1,
                        10,
                        null,
                        null,
                        null
                );

        StoredJobPosting storedJob =
                store.find(criteria).get(0);

        assertThat(storedJob.relevance())
                .isEqualTo(relevance);

        assertThat(storedJob.scoredAt())
                .isNotNull();
    }

    @Test
    void ordersJobsByRelevanceWhenRequested() {
        JobPosting olderRelevantJob = new JobPosting(
                "Arbeitnow",
                "high-relevance",
                "Senior Data Engineer",
                "Example Company",
                "Berlin",
                false,
                URI.create(
                        "https://example.com/jobs/high-relevance"
                ),
                Instant.parse("2026-09-20T10:00:00Z")
        );

        JobPosting newerLessRelevantJob = new JobPosting(
                "Arbeitnow",
                "low-relevance",
                "Backend Developer",
                "Example Company",
                "Remote",
                true,
                URI.create(
                        "https://example.com/jobs/low-relevance"
                ),
                Instant.parse("2026-09-23T10:00:00Z")
        );

        JobRelevanceScore highRelevance =
                new JobRelevanceScore(
                        80,
                        45,
                        25,
                        10,
                        0,
                        List.of("data engineer"),
                        List.of("sql", "python"),
                        List.of("full-time")
                );

        JobRelevanceScore lowRelevance =
                new JobRelevanceScore(
                        20,
                        0,
                        15,
                        0,
                        5,
                        List.of(),
                        List.of("java"),
                        List.of()
                );

        store.upsertAllScored(List.of(
                new ScoredJobPosting(
                        olderRelevantJob,
                        highRelevance
                ),
                new ScoredJobPosting(
                        newerLessRelevantJob,
                        lowRelevance
                )
        ));

        JobSearchCriteria criteria =
                new JobSearchCriteria(
                        1,
                        10,
                        null,
                        null,
                        null,
                        JobSort.RELEVANCE
                );

        assertThat(store.find(criteria))
                .extracting(StoredJobPosting::sourceId)
                .containsExactly(
                        "high-relevance",
                        "low-relevance"
                );
    }
}
