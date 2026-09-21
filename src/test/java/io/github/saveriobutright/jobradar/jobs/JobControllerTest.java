package io.github.saveriobutright.jobradar.jobs;

import io.github.saveriobutright.jobradar.sources.JobSource;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JobControllerTest {

    @Test
    void returnsJobsForRequestedPage() throws Exception {
        JobPosting job = new JobPosting(
                "TestSource",
                "test-1",
                "Data Engineer",
                "Example Company",
                "Remote",
                true,
                URI.create("https://example.com/jobs/test-1"),
                Instant.parse("2026-09-20T08:00:00Z")
        );

        JobSource source = pageNumber -> {
            assertThat(pageNumber).isEqualTo(2);
            return List.of(job);
        };

        JobSearchService service = new JobSearchService(
                List.of(source)
        );

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new JobController(service))
                .build();

        mockMvc.perform(
                        get("/api/jobs")
                                .queryParam("page", "2")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$[0].source")
                        .value("TestSource"))
                .andExpect(jsonPath("$[0].sourceId")
                        .value("test-1"))
                .andExpect(jsonPath("$[0].title")
                        .value("Data Engineer"))
                .andExpect(jsonPath("$[0].remote")
                        .value(true));
    }
}