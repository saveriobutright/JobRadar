package io.github.saveriobutright.jobradar.sources.arbeitnow;

import io.github.saveriobutright.jobradar.jobs.JobPosting;
import io.github.saveriobutright.jobradar.sources.JobSource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

@Component
public final class ArbeitnowClient implements JobSource {

    private static final String BASE_URL = "https://www.arbeitnow.com";

    private final RestClient restClient;
    private final ArbeitnowJobMapper mapper;

    public ArbeitnowClient(
            RestClient.Builder restClientBuilder,
            ArbeitnowJobMapper mapper
    ) {
        this.restClient = restClientBuilder
                .baseUrl(BASE_URL)
                .build();
        this.mapper = mapper;
    }

    @Override
    public List<JobPosting> fetchPage(int pageNumber) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException(
                    "pageNumber must be at least 1"
            );
        }

        ArbeitnowPage response = Objects.requireNonNull(
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/job-board-api")
                                .queryParam("page", pageNumber)
                                .build())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(ArbeitnowPage.class),
                "Arbeitnow returned an empty response"
        );

        List<ArbeitnowJob> jobs = Objects.requireNonNull(
                response.data(),
                "Arbeitnow response data must not be null"
        );

        return jobs.stream()
                .map(mapper::map)
                .toList();
    }
}
