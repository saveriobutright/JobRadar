package io.github.saveriobutright.jobradar.jobs.scoring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("jobradar.scoring")
public record JobScoringProperties(
        List<String> targetRoles,
        List<String> skills,
        List<String> preferredJobTypes,
        boolean remotePreferred
) {

    public JobScoringProperties {
        JobRelevanceProfile normalizedProfile =
                new JobRelevanceProfile(
                        targetRoles,
                        skills,
                        preferredJobTypes,
                        remotePreferred
                );

        targetRoles = normalizedProfile.targetRoles();
        skills = normalizedProfile.skills();
        preferredJobTypes =
                normalizedProfile.preferredJobTypes();
    }

    public JobRelevanceProfile toProfile() {
        return new JobRelevanceProfile(
                targetRoles,
                skills,
                preferredJobTypes,
                remotePreferred
        );
    }
}
