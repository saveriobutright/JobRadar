package io.github.saveriobutright.jobradar.jobs;

import java.util.Arrays;
import java.util.Locale;

public enum JobSort {

    NEWEST("newest"),
    RELEVANCE("relevance");

    private final String value;

    JobSort(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static JobSort from(String value) {
        if (value == null || value.isBlank()) {
            return NEWEST;
        }

        String normalized =
                value.trim().toLowerCase(Locale.ROOT);

        return Arrays.stream(values())
                .filter(sort ->
                        sort.value.equals(normalized)
                )
                .findFirst()
                .orElseThrow(() ->
                        new InvalidSearchCriteriaException(
                                "sort must be one of: "
                                        + "newest, relevance"
                        )
                );
    }
}
