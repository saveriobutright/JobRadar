package io.github.saveriobutright.jobradar.jobs;

import io.github.saveriobutright.jobradar.jobs.persistence.JobPostingStore;
import io.github.saveriobutright.jobradar.jobs.scoring.JobRelevanceProfile;
import io.github.saveriobutright.jobradar.jobs.scoring.JobRelevanceScore;
import io.github.saveriobutright.jobradar.jobs.scoring.JobRelevanceScorer;
import io.github.saveriobutright.jobradar.jobs.scoring.ScoredJobPosting;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobIngestionServiceTest {

    @Test
    void scoresAndPersistsFetchedJobs() {
        JobPosting job = new JobPosting(
                "Arbeitnow",
                "data-engineer-example",
                "Data Engineer",
                "Example Company",
                "Berlin",
                false,
                URI.create(
                        "https://example.com/jobs/data-engineer"
                ),
                Instant.parse("2026-09-21T08:00:00Z")
        );

        JobRelevanceProfile profile =
                new JobRelevanceProfile(
                        List.of("Data Engineer"),
                        List.of("SQL"),
                        List.of(),
                        false
                );

        JobRelevanceScore relevance =
                new JobRelevanceScore(
                        45,
                        45,
                        0,
                        0,
                        0,
                        List.of("data engineer"),
                        List.of(),
                        List.of()
                );

        ScoredJobPosting scoredJob =
                new ScoredJobPosting(
                        job,
                        relevance
                );

        JobSearchService jobSearchService =
                mock(JobSearchService.class);

        JobPostingStore jobPostingStore =
                mock(JobPostingStore.class);

        JobRelevanceScorer relevanceScorer =
                mock(JobRelevanceScorer.class);

        when(jobSearchService.findJobs(2))
                .thenReturn(List.of(job));

        when(relevanceScorer.score(job, profile))
                .thenReturn(relevance);

        when(jobPostingStore.upsertAllScored(
                List.of(scoredJob)
        )).thenReturn(1);

        JobIngestionService service =
                new JobIngestionService(
                        jobSearchService,
                        jobPostingStore,
                        relevanceScorer,
                        profile
                );

        int affectedRows = service.ingestPage(2);

        assertThat(affectedRows).isEqualTo(1);

        verify(jobSearchService).findJobs(2);
        verify(relevanceScorer).score(job, profile);

        verify(jobPostingStore).upsertAllScored(
                List.of(scoredJob)
        );
    }
}
