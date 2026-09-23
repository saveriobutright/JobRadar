package io.github.saveriobutright.jobradar.sources.arbeitnow;

import io.github.saveriobutright.jobradar.jobs.JobPosting;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArbeitnowJobMapperTest {

    private final ArbeitnowJobMapper mapper = new ArbeitnowJobMapper();

    @Test
    void mapsArbeitnowJobToNormalizedJobPosting() {
        ArbeitnowJob sourceJob = new ArbeitnowJob(
                "data-engineer-example",
                "Example Company",
                "Data Engineer",
                "<p>Build reliable data pipelines "
                        + "with Java &amp; SQL.</p>",
                true,
                "https://www.arbeitnow.com/jobs/data-engineer-example",
                List.of(" IT ", "Data", "IT", ""),
                List.of("Full-time"),
                "Berlin",
                1720000000L
        );

        JobPosting result = mapper.map(sourceJob);

        JobPosting expected = new JobPosting(
                "Arbeitnow",
                "data-engineer-example",
                "Data Engineer",
                "Example Company",
                "Build reliable data pipelines with Java & SQL.",
                List.of("IT", "Data"),
                List.of("Full-time"),
                "Berlin",
                true,
                URI.create(
                        "https://www.arbeitnow.com/jobs/"
                                + "data-engineer-example"
                ),
                Instant.ofEpochSecond(1720000000L)
        );

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void mapsMissingOptionalContentToEmptyValues() {
        ArbeitnowJob sourceJob = new ArbeitnowJob(
                "minimal-job",
                "Example Company",
                "Data Engineer",
                null,
                false,
                "https://www.arbeitnow.com/jobs/minimal-job",
                null,
                null,
                "Rome",
                1720000000L
        );

        JobPosting result = mapper.map(sourceJob);

        assertThat(result.description()).isEmpty();
        assertThat(result.tags()).isEmpty();
        assertThat(result.jobTypes()).isEmpty();
    }
}
