package io.github.saveriobutright.jobradar.jobs.scheduling;

import io.github.saveriobutright.jobradar.jobs.JobIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "jobradar.ingestion.schedule",
        name = "enabled",
        havingValue = "true"
)
public final class JobIngestionScheduler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    JobIngestionScheduler.class
            );

    private final JobIngestionService jobIngestionService;
    private final JobIngestionScheduleProperties properties;

    public JobIngestionScheduler(
            JobIngestionService jobIngestionService,
            JobIngestionScheduleProperties properties
    ) {
        this.jobIngestionService = jobIngestionService;
        this.properties = properties;
    }

    @Scheduled(
            fixedDelayString =
                    "${jobradar.ingestion.schedule.fixed-delay}",
            initialDelayString =
                    "${jobradar.ingestion.schedule.initial-delay}"
    )
    public void ingestConfiguredPage() {
        int page = properties.page();

        LOGGER.info(
                "Starting scheduled job ingestion for page {}",
                page
        );

        int processedJobs =
                jobIngestionService.ingestPage(page);

        LOGGER.info(
                "Completed scheduled job ingestion for page {}: "
                        + "{} jobs processed",
                page,
                processedJobs
        );
    }
}
