package io.github.saveriobutright.jobradar.sources.arbeitnow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ArbeitnowJob(
        String slug,
        @JsonProperty("company_name") String companyName,
        String title,
        String location,
        Boolean remote,
        String url,
        @JsonProperty("created_at") Long createdAt
) {}
