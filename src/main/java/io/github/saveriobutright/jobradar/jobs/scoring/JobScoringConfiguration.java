package io.github.saveriobutright.jobradar.jobs.scoring;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JobScoringProperties.class)
public class JobScoringConfiguration {

    @Bean
    public JobRelevanceProfile jobRelevanceProfile(
            JobScoringProperties properties
    ) {
        return properties.toProfile();
    }

    @Bean
    public JobRelevanceScorer jobRelevanceScorer() {
        return new JobRelevanceScorer();
    }
}
