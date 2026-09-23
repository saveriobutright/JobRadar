package io.github.saveriobutright.jobradar.jobs.scoring;

import io.github.saveriobutright.jobradar.jobs.JobPosting;

import java.util.Objects;

public record ScoredJobPosting(
        JobPosting job,
        JobRelevanceScore relevance
) {

    public ScoredJobPosting {
        Objects.requireNonNull(
                job,
                "job must not be null"
        );

        Objects.requireNonNull(
                relevance,
                "relevance must not be null"
        );
    }
}
