package io.github.saveriobutright.jobradar.jobs;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JobIngestionControllerTest {

    @Test
    void ingestsRequestedPageAndReturnsResult() throws Exception {
        JobIngestionService service =
                mock(JobIngestionService.class);

        when(service.ingestPage(2))
                .thenReturn(250);

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new JobIngestionController(service)
                )
                .build();

        mockMvc.perform(
                        post("/api/ingestions/jobs")
                                .queryParam("page", "2")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.processedJobs").value(250));

        verify(service).ingestPage(2);
    }
}