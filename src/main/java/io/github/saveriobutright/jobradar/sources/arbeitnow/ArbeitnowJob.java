package io.github.saveriobutright.jobradar.sources.arbeitnow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ArbeitnowJob(
        String slug,
        @JsonProperty("company_name") String companyName,
        String title,
        String description,
        Boolean remote,
        String url,
        List<String> tags,
        @JsonProperty("job_types") List<String> jobTypes,
        String location,
        @JsonProperty("created_at") Long createdAt
) {
}
