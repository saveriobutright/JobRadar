package io.github.saveriobutright.jobradar.jobs;

import io.github.saveriobutright.jobradar.api.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JobControllerTest {

    @Test
    void returnsFilteredStoredJobsWithPaginationMetadata()
            throws Exception {
        JobSearchCriteria criteria = new JobSearchCriteria(
                2,
                10,
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

        JobSearchResult result = new JobSearchResult(
                List.of(storedJob),
                2,
                10,
                21,
                3
        );

        StoredJobSearchService service =
                mock(StoredJobSearchService.class);

        when(service.search(criteria)).thenReturn(result);

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new JobController(service))
                .build();

        mockMvc.perform(
                        get("/api/jobs")
                                .queryParam("page", "2")
                                .queryParam("size", "10")
                                .queryParam("query", "data")
                                .queryParam("location", "berlin")
                                .queryParam("remote", "true")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(21))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.items[0].id").value(42))
                .andExpect(jsonPath("$.items[0].source")
                        .value("Arbeitnow"))
                .andExpect(jsonPath("$.items[0].sourceId")
                        .value("data-engineer-example"))
                .andExpect(jsonPath("$.items[0].title")
                        .value("Data Engineer"))
                .andExpect(jsonPath("$.items[0].remote")
                        .value(true));

        verify(service).search(criteria);
    }

    @Test
    void returnsBadRequestForInvalidPagination()
            throws Exception {
        StoredJobSearchService service =
                mock(StoredJobSearchService.class);

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new JobController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        mockMvc.perform(
                        get("/api/jobs")
                                .queryParam("page", "0")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title")
                        .value("Invalid request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail")
                        .value("page must be at least 1"))
                .andExpect(jsonPath("$.instance")
                        .value("/api/jobs"));

        verifyNoInteractions(service);
    }
}
