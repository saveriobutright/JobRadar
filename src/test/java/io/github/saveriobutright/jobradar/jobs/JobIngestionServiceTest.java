package io.github.saveriobutright.jobradar.jobs;

import io.github.saveriobutright.jobradar.jobs.persistence.JobPostingStore;
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
    void persistsFetchedJobsAndReturnsAffectedRowCount() {
        JobPosting job = new JobPosting(
                "Arbeitnow",
                "data-engineer-example",
                "Data Engineer",
                "Example Company",
                "Berlin",
                false,
                URI.create("https://example.com/jobs/data-engineer"),
                Instant.parse("2026-09-21T08:00:00Z")
        );

        List<JobPosting> jobs = List.of(job);

        JobSearchService jobSearchService =
                mock(JobSearchService.class);
        JobPostingStore jobPostingStore =
                mock(JobPostingStore.class);

        when(jobSearchService.findJobs(2))
                .thenReturn(jobs);
        when(jobPostingStore.upsertAll(jobs))
                .thenReturn(1);

        JobIngestionService service = new JobIngestionService(
                jobSearchService,
                jobPostingStore
        );

        int affectedRows = service.ingestPage(2);

        assertThat(affectedRows).isEqualTo(1);
        verify(jobSearchService).findJobs(2);
        verify(jobPostingStore).upsertAll(jobs);
    }
}
