package io.github.saveriobutright.jobradar.jobs;

import io.github.saveriobutright.jobradar.jobs.scoring.JobRelevanceScore;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record StoredJobPosting(
        long id,
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
        Instant postedAt,
        Instant firstSeenAt,
        Instant lastSeenAt,
        JobRelevanceScore relevance,
        Instant scoredAt
) {

    public StoredJobPosting {
        description = description == null
                ? ""
                : description;

        tags = tags == null
                ? List.of()
                : List.copyOf(tags);

        jobTypes = jobTypes == null
                ? List.of()
                : List.copyOf(jobTypes);

        Objects.requireNonNull(
                relevance,
                "relevance must not be null"
        );
    }

    public StoredJobPosting(
            long id,
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
            Instant postedAt,
            Instant firstSeenAt,
            Instant lastSeenAt
    ) {
        this(
                id,
                source,
                sourceId,
                title,
                company,
                description,
                tags,
                jobTypes,
                location,
                remote,
                sourceUrl,
                postedAt,
                firstSeenAt,
                lastSeenAt,
                unscoredRelevance(),
                null
        );
    }

    public StoredJobPosting(
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
        this(
                id,
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
                postedAt,
                firstSeenAt,
                lastSeenAt
        );
    }

    private static JobRelevanceScore unscoredRelevance() {
        return new JobRelevanceScore(
                0,
                0,
                0,
                0,
                0,
                List.of(),
                List.of(),
                List.of()
        );
    }
}
