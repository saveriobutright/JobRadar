package io.github.saveriobutright.jobradar.sources.arbeitnow;

import io.github.saveriobutright.jobradar.jobs.JobPosting;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;

@Component
public final class ArbeitnowJobMapper {

    public JobPosting map(ArbeitnowJob job) {
        Objects.requireNonNull(job, "job must not be null");

        String sourceId = Objects.requireNonNull(
                job.slug(),
                "Arbeitnow slug must not be null"
        );

        URI sourceUrl = URI.create(Objects.requireNonNull(
                job.url(),
                "Arbeitnow URL must not be null"
        ));

        Instant postedAt = Instant.ofEpochSecond(Objects.requireNonNull(
                job.createdAt(),
                "Arbeitnow created_at must not be null"
        ));

        return new JobPosting(
                "Arbeitnow",
                sourceId,
                job.title(),
                job.companyName(),
                job.location(),
                Boolean.TRUE.equals(job.remote()),
                sourceUrl,
                postedAt
        );
    }
}
