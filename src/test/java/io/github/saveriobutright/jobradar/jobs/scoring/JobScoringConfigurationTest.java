package io.github.saveriobutright.jobradar.jobs.scoring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class JobScoringConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(
                            JobScoringConfiguration.class
                    )
                    .withPropertyValues(
                            "jobradar.scoring.target-roles="
                                    + "Data Engineer,AI Engineer",
                            "jobradar.scoring.skills="
                                    + "SQL,Java,Kafka",
                            "jobradar.scoring."
                                    + "preferred-job-types="
                                    + "Full-time,Contract",
                            "jobradar.scoring."
                                    + "remote-preferred=true"
                    );

    @Test
    void bindsAndNormalizesConfiguredProfile() {
        contextRunner.run(context -> {
            assertThat(context)
                    .hasSingleBean(
                            JobScoringProperties.class
                    )
                    .hasSingleBean(
                            JobRelevanceProfile.class
                    )
                    .hasSingleBean(
                            JobRelevanceScorer.class
                    );

            JobRelevanceProfile profile =
                    context.getBean(
                            JobRelevanceProfile.class
                    );

            assertThat(profile.targetRoles())
                    .containsExactly(
                            "data engineer",
                            "ai engineer"
                    );

            assertThat(profile.skills())
                    .containsExactly(
                            "sql",
                            "java",
                            "kafka"
                    );

            assertThat(profile.preferredJobTypes())
                    .containsExactly(
                            "full-time",
                            "contract"
                    );

            assertThat(profile.remotePreferred())
                    .isTrue();
        });
    }
}
