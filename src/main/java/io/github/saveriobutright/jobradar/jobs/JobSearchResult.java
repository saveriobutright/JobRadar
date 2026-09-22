package io.github.saveriobutright.jobradar.jobs;

import java.util.List;
import java.util.Objects;

public record JobSearchResult(
        List<StoredJobPosting> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {

    public JobSearchResult {
        items = List.copyOf(
                Objects.requireNonNull(items, "items must not be null")
        );
    }

    public static JobSearchResult from(
            List<StoredJobPosting> items,
            JobSearchCriteria criteria,
            long totalItems
    ) {
        Objects.requireNonNull(
                criteria,
                "criteria must not be null"
        );

        if (totalItems < 0) {
            throw new IllegalArgumentException(
                    "totalItems must not be negative"
            );
        }

        int totalPages = totalItems == 0
                ? 0
                : Math.toIntExact(
                        ((totalItems - 1) / criteria.size()) + 1
                );

        return new JobSearchResult(
                items,
                criteria.page(),
                criteria.size(),
                totalItems,
                totalPages
        );
    }
}
