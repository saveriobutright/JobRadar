package io.github.saveriobutright.jobradar.jobs.scoring;

import io.github.saveriobutright.jobradar.jobs.JobPosting;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class JobRelevanceScorer {

    private static final int ROLE_POINTS = 45;
    private static final int SKILL_POINTS = 40;
    private static final int JOB_TYPE_POINTS = 10;
    private static final int REMOTE_POINTS = 5;

    public JobRelevanceScore score(
            JobPosting job,
            JobRelevanceProfile profile
    ) {
        Objects.requireNonNull(job, "job must not be null");
        Objects.requireNonNull(
                profile,
                "profile must not be null"
        );

        List<String> matchedRoles = findKeywords(
                profile.targetRoles(),
                safe(job.title())
        );

        String skillText = String.join(
                "\n",
                safe(job.title()),
                safe(job.description()),
                String.join("\n", job.tags())
        );

        List<String> matchedSkills = findKeywords(
                profile.skills(),
                skillText
        );

        List<String> matchedJobTypes =
                findExactValues(
                        profile.preferredJobTypes(),
                        job.jobTypes()
                );

        int rolePoints = matchedRoles.isEmpty()
                ? 0
                : ROLE_POINTS;

        int skillPoints = proportionalPoints(
                SKILL_POINTS,
                matchedSkills.size(),
                profile.skills().size()
        );

        int jobTypePoints = matchedJobTypes.isEmpty()
                ? 0
                : JOB_TYPE_POINTS;

        int remotePoints =
                profile.remotePreferred() && job.remote()
                        ? REMOTE_POINTS
                        : 0;

        int totalScore = rolePoints
                + skillPoints
                + jobTypePoints
                + remotePoints;

        return new JobRelevanceScore(
                totalScore,
                rolePoints,
                skillPoints,
                jobTypePoints,
                remotePoints,
                matchedRoles,
                matchedSkills,
                matchedJobTypes
        );
    }

    private static List<String> findKeywords(
            List<String> keywords,
            String text
    ) {
        return keywords.stream()
                .filter(keyword ->
                        containsKeyword(text, keyword)
                )
                .toList();
    }

    private static boolean containsKeyword(
            String text,
            String keyword
    ) {
        Pattern pattern = Pattern.compile(
                "(?<![\\p{L}\\p{N}])"
                        + Pattern.quote(keyword)
                        + "(?![\\p{L}\\p{N}])",
                Pattern.CASE_INSENSITIVE
                        | Pattern.UNICODE_CASE
        );

        return pattern.matcher(text).find();
    }

    private static List<String> findExactValues(
            List<String> expectedValues,
            List<String> actualValues
    ) {
        List<String> normalizedActualValues =
                actualValues.stream()
                        .map(JobRelevanceScorer::normalize)
                        .toList();

        return expectedValues.stream()
                .filter(normalizedActualValues::contains)
                .toList();
    }

    private static int proportionalPoints(
            int maximumPoints,
            int matchedValues,
            int totalValues
    ) {
        if (totalValues == 0) {
            return 0;
        }

        return (int) Math.round(
                (double) maximumPoints
                        * matchedValues
                        / totalValues
        );
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
