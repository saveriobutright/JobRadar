package io.github.saveriobutright.jobradar.jobs;

import java.net.URI;
import java.time.Instant;

public record StoredJobPosting(
        long id,
        String source,
        String sourceId,
        String title,
        String company,
        String location,
        boolean remote,
        URI sourceUrl,
        Instant postedAt,
        Instant firstSeenAt,
        Instant lastSeenAt
) {
}
