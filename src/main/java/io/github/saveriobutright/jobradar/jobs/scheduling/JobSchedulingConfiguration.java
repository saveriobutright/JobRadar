package io.github.saveriobutright.jobradar.jobs.scheduling;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@EnableConfigurationProperties(
        JobIngestionScheduleProperties.class
)
public class JobSchedulingConfiguration {
}
