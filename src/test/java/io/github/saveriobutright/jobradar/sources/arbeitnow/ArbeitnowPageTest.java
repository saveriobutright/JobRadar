package io.github.saveriobutright.jobradar.sources.arbeitnow;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class ArbeitnowPageTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void mapsArbeitnowJsonToPageRecords() throws Exception {
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
                      "created_at": 1720000000,
                      "description": "<p>Build reliable data pipelines with Java and SQL.</p>",
                      "tags": ["IT", "Data"],
                      "job_types": ["Full-time"]
                    }
                  ],
                  "links": {
                    "next": "https://www.arbeitnow.com/api/job-board-api?page=2",
                    "first": "https://www.arbeitnow.com/api/job-board-api?page=1"
                  },
                  "meta": {
                    "current_page": 1
                  }
                }
                """;

        ArbeitnowPage page = jsonMapper.readValue(json, ArbeitnowPage.class);

        assertThat(page.data()).hasSize(1);

        ArbeitnowJob job = page.data().get(0);

        assertThat(page.links().next()).endsWith("page=2");
        assertThat(job.slug()).isEqualTo("data-engineer-example");
        assertThat(job.companyName()).isEqualTo("Example Company");
        assertThat(job.description()).contains("reliable data pipelines");
        assertThat(job.tags()).containsExactly("IT", "Data");
        assertThat(job.jobTypes()).containsExactly("Full-time");
        assertThat(job.remote()).isTrue();
        assertThat(job.createdAt()).isEqualTo(1720000000L);
    }
}
