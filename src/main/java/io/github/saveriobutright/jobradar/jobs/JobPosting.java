package io.github.saveriobutright.jobradar.jobs;

import java.net.URI;
import java.time.Instant;

public record JobPosting(
        String source,
        String sourceId,
        String title,
        String company,
        String location,
        boolean remote,
        URI sourceUrl,
        Instant postedAt
){}