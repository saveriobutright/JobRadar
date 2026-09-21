package io.github.saveriobutright.jobradar.sources.arbeitnow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ArbeitnowPage(
        List<ArbeitnowJob> data,
        Links links
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Links(String next) {
    }
}
