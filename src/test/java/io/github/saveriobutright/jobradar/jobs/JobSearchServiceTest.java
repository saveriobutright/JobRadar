package io.github.saveriobutright.jobradar.jobs;

import io.github.saveriobutright.jobradar.sources.JobSource;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobSearchServiceTest {

    @Test
    void aggregatesJobsFromEverySource() {
        JobPosting firstJob = new JobPosting(
                "FirstSource",
                "first-1",
                "Data Engineer",
                "Example One",
                "Berlin",
                true,
                URI.create("https://example.com/jobs/first-1"),
                Instant.parse("2026-09-20T08:00:00Z")
        );

        JobPosting secondJob = new JobPosting(
                "SecondSource",
                "second-1",
                "Machine Learning Engineer",
                "Example Two",
                "Paris",
                false,
                URI.create("https://example.com/jobs/second-1"),
                Instant.parse("2026-09-20T09:00:00Z")
        );

        JobSource firstSource = pageNumber -> {
            assertThat(pageNumber).isEqualTo(2);
            return List.of(firstJob);
        };

        JobSource secondSource = pageNumber -> {
            assertThat(pageNumber).isEqualTo(2);
            return List.of(secondJob);
        };

        JobSearchService service = new JobSearchService(
                List.of(firstSource, secondSource)
        );

        List<JobPosting> jobs = service.findJobs(2);

        assertThat(jobs).containsExactly(firstJob, secondJob);
    }
}