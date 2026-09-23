package io.github.saveriobutright.jobradar.jobs;

public record JobSearchCriteria(
        int page,
        int size,
        String query,
        String location,
        Boolean remote,
        JobSort sort
) {

    public JobSearchCriteria {
        if (page < 1) {
            throw new InvalidSearchCriteriaException(
                    "page must be at least 1"
            );
        }

        if (size < 1 || size > 100) {
            throw new InvalidSearchCriteriaException(
                    "size must be between 1 and 100"
            );
        }

        query = normalize(query);
        location = normalize(location);

        sort = sort == null
                ? JobSort.NEWEST
                : sort;
    }

    public JobSearchCriteria(
            int page,
            int size,
            String query,
            String location,
            Boolean remote
    ) {
        this(
                page,
                size,
                query,
                location,
                remote,
                JobSort.NEWEST
        );
    }

    public long offset() {
        return (long) (page - 1) * size;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}
