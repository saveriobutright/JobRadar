package io.github.saveriobutright.jobradar.jobs;

import java.net.URI;
import java.time.Instant;
import java.util.List;

public record JobPosting(
        String source,
        String sourceId,
        String title,
        String company,
        String description,
        List<String> tags,
        List<String> jobTypes,
        String location,
        boolean remote,
        URI sourceUrl,
        Instant postedAt
) {

    public JobPosting {
        description = description == null
                ? ""
                : description;

        tags = tags == null
                ? List.of()
                : List.copyOf(tags);

        jobTypes = jobTypes == null
                ? List.of()
                : List.copyOf(jobTypes);
    }

    public JobPosting(
            String source,
            String sourceId,
            String title,
            String company,
            String location,
            boolean remote,
            URI sourceUrl,
            Instant postedAt
    ) {
        this(
                source,
                sourceId,
                title,
                company,
                "",
                List.of(),
                List.of(),
                location,
                remote,
                sourceUrl,
                postedAt
        );
    }
}
