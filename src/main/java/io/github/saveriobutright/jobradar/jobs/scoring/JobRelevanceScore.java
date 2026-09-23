package io.github.saveriobutright.jobradar.jobs.scoring;

import java.util.List;
import java.util.Objects;

public record JobRelevanceScore(
        int score,
        int rolePoints,
        int skillPoints,
        int jobTypePoints,
        int remotePoints,
        List<String> matchedRoles,
        List<String> matchedSkills,
        List<String> matchedJobTypes
) {

    public JobRelevanceScore {
        if (rolePoints < 0
                || skillPoints < 0
                || jobTypePoints < 0
                || remotePoints < 0) {
            throw new IllegalArgumentException(
                    "score components must not be negative"
            );
        }

        int calculatedScore = rolePoints
                + skillPoints
                + jobTypePoints
                + remotePoints;

        if (score != calculatedScore) {
            throw new IllegalArgumentException(
                    "score must equal the sum of its components"
            );
        }

        if (score > 100) {
            throw new IllegalArgumentException(
                    "score must not exceed 100"
            );
        }

        matchedRoles = copyOf(
                matchedRoles,
                "matchedRoles"
        );

        matchedSkills = copyOf(
                matchedSkills,
                "matchedSkills"
        );

        matchedJobTypes = copyOf(
                matchedJobTypes,
                "matchedJobTypes"
        );
    }

    private static List<String> copyOf(
            List<String> values,
            String fieldName
    ) {
        return List.copyOf(
                Objects.requireNonNull(
                        values,
                        fieldName + " must not be null"
                )
        );
    }
}
