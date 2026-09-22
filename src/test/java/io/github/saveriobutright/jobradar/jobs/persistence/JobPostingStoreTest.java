package io.github.saveriobutright.jobradar.jobs.persistence;

import io.github.saveriobutright.jobradar.TestcontainersConfiguration;
import io.github.saveriobutright.jobradar.jobs.JobPosting;
import io.github.saveriobutright.jobradar.jobs.JobSearchCriteria;
import io.github.saveriobutright.jobradar.jobs.StoredJobPosting;
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
}
