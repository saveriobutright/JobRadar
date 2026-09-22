package io.github.saveriobutright.jobradar.jobs.scheduling;

import io.github.saveriobutright.jobradar.jobs.JobIngestionService;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobIngestionSchedulerTest {

    @Test
    void ingestsConfiguredPage() {
        JobIngestionService ingestionService =
                mock(JobIngestionService.class);

        JobIngestionScheduleProperties properties =
                new JobIngestionScheduleProperties(
                        true,
                        2,
                        Duration.ofHours(1),
                        Duration.ofSeconds(30)
                );

        when(ingestionService.ingestPage(2))
                .thenReturn(250);

        JobIngestionScheduler scheduler =
                new JobIngestionScheduler(
                        ingestionService,
                        properties
                );

        scheduler.ingestConfiguredPage();

        verify(ingestionService).ingestPage(2);
    }
}
