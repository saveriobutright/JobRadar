package io.github.saveriobutright.jobradar.jobs.scoring;

import io.github.saveriobutright.jobradar.jobs.JobPosting;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobRelevanceScorerTest {

    private final JobRelevanceScorer scorer =
            new JobRelevanceScorer();

    @Test
    void calculatesExplainableScoreFromProfileMatches() {
        JobRelevanceProfile profile =
                new JobRelevanceProfile(
                        List.of(
                                " Data Engineer ",
                                "data engineer",
                                "AI Engineer"
                        ),
                        List.of(
                                "Java",
                                "SQL",
                                "Kafka",
                                "Python"
                        ),
                        List.of("Full-time"),
                        true
                );

        JobPosting job = new JobPosting(
                "Arbeitnow",
                "senior-data-engineer",
                "Senior Data Engineer",
                "Example Company",
                "Build streaming platforms "
                        + "with Java and SQL.",
                List.of("Kafka", "IT"),
                List.of("Full-time"),
                "Remote",
                true,
                URI.create(
                        "https://example.com/jobs/"
                                + "senior-data-engineer"
                ),
                Instant.parse("2026-09-23T10:00:00Z")
        );

        JobRelevanceScore result =
                scorer.score(job, profile);

        assertThat(result.score()).isEqualTo(90);
        assertThat(result.rolePoints()).isEqualTo(45);
        assertThat(result.skillPoints()).isEqualTo(30);
        assertThat(result.jobTypePoints()).isEqualTo(10);
        assertThat(result.remotePoints()).isEqualTo(5);

        assertThat(result.matchedRoles())
                .containsExactly("data engineer");

        assertThat(result.matchedSkills())
                .containsExactly("java", "sql", "kafka");

        assertThat(result.matchedJobTypes())
                .containsExactly("full-time");
    }

    @Test
    void doesNotMatchKeywordInsideAnotherWord() {
        JobRelevanceProfile profile =
                new JobRelevanceProfile(
                        List.of("AI Engineer"),
                        List.of("AI"),
                        List.of(),
                        false
                );

        JobPosting job = new JobPosting(
                "Arbeitnow",
                "maintenance-engineer",
                "Maintenance Engineer",
                "Example Company",
                "Maintain internal services.",
                List.of(),
                List.of(),
                "Rome",
                false,
                URI.create(
                        "https://example.com/jobs/"
                                + "maintenance-engineer"
                ),
                Instant.parse("2026-09-23T10:00:00Z")
        );

        JobRelevanceScore result =
                scorer.score(job, profile);

        assertThat(result.score()).isZero();
        assertThat(result.matchedRoles()).isEmpty();
        assertThat(result.matchedSkills()).isEmpty();
    }
}
