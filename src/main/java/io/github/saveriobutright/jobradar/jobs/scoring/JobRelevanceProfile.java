package io.github.saveriobutright.jobradar.jobs.scoring;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public record JobRelevanceProfile(
        List<String> targetRoles,
        List<String> skills,
        List<String> preferredJobTypes,
        boolean remotePreferred
) {

    public JobRelevanceProfile {
        targetRoles = normalize(
                targetRoles,
                "targetRoles"
        );

        skills = normalize(
                skills,
                "skills"
        );

        preferredJobTypes = normalize(
                preferredJobTypes,
                "preferredJobTypes"
        );
    }

    private static List<String> normalize(
            List<String> values,
            String fieldName
    ) {
        Objects.requireNonNull(
                values,
                fieldName + " must not be null"
        );

        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
    }
}
