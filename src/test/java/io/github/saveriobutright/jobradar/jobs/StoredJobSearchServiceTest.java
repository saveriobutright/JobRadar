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

class StoredJobSearchServiceTest {

    @Test
    void returnsStoredJobsWithPaginationMetadata() {
        JobSearchCriteria criteria = new JobSearchCriteria(
                2,
                2,
                "data",
                "berlin",
                true
        );

        StoredJobPosting storedJob = new StoredJobPosting(
                42,
                "Arbeitnow",
                "data-engineer-example",
                "Data Engineer",
                "Example Company",
                "Berlin",
                true,
                URI.create(
                        "https://example.com/jobs/data-engineer"
                ),
                Instant.parse("2026-09-21T08:00:00Z"),
                Instant.parse("2026-09-21T09:00:00Z"),
                Instant.parse("2026-09-22T09:00:00Z")
        );

        JobPostingStore store = mock(JobPostingStore.class);

        when(store.count(criteria)).thenReturn(5L);
        when(store.find(criteria))
                .thenReturn(List.of(storedJob));

        StoredJobSearchService service =
                new StoredJobSearchService(store);

        JobSearchResult result = service.search(criteria);

        assertThat(result.items())
                .containsExactly(storedJob);
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalItems()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);

        verify(store).count(criteria);
        verify(store).find(criteria);
    }
}
