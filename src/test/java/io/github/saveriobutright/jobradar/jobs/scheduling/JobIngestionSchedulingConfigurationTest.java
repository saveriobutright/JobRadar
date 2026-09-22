package io.github.saveriobutright.jobradar.jobs.scheduling;

import io.github.saveriobutright.jobradar.jobs.JobIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JobIngestionSchedulingConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(
                            JobSchedulingConfiguration.class,
                            JobIngestionScheduler.class,
                            TestConfiguration.class
                    )
                    .withPropertyValues(
                            "jobradar.ingestion.schedule.page=1",
                            "jobradar.ingestion.schedule."
                                    + "fixed-delay=PT1H",
                            "jobradar.ingestion.schedule."
                                    + "initial-delay=PT24H"
                    );

    @Test
    void doesNotCreateSchedulerWhenDisabled() {
        contextRunner
                .withPropertyValues(
                        "jobradar.ingestion.schedule.enabled=false"
                )
                .run(context -> assertThat(context)
                        .doesNotHaveBean(
                                JobIngestionScheduler.class
                        ));
    }

    @Test
    void createsSchedulerWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "jobradar.ingestion.schedule.enabled=true"
                )
                .run(context -> assertThat(context)
                        .hasSingleBean(
                                JobIngestionScheduler.class
                        ));
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {

        @Bean
        JobIngestionService jobIngestionService() {
            return mock(JobIngestionService.class);
        }
    }
}
