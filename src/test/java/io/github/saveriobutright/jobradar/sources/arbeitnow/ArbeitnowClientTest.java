package io.github.saveriobutright.jobradar.sources.arbeitnow;

import io.github.saveriobutright.jobradar.jobs.JobPosting;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ArbeitnowClientTest {

    @Test
    void fetchesAndNormalizesRequestedPage() {
        RestClient.Builder restClientBuilder = RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(restClientBuilder).build();

        ArbeitnowClient client = new ArbeitnowClient(
                restClientBuilder,
                new ArbeitnowJobMapper()
        );

        String json = """
                {
                  "data": [
                    {
                      "slug": "data-engineer-example",
                      "company_name": "Example Company",
                      "title": "Data Engineer",
                      "location": "Berlin",
                      "remote": true,
                      "url": "https://www.arbeitnow.com/jobs/data-engineer-example",
                      "created_at": 1720000000
                    }
                  ],
                  "links": {
                    "next": null
                  }
                }
                """;

        server.expect(requestTo(
                        "https://www.arbeitnow.com/api/job-board-api?page=2"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<JobPosting> jobs = client.fetchPage(2);

        assertThat(jobs).hasSize(1);
        assertThat(jobs.get(0).source()).isEqualTo("Arbeitnow");
        assertThat(jobs.get(0).sourceId())
                .isEqualTo("data-engineer-example");

        server.verify();
    }
}