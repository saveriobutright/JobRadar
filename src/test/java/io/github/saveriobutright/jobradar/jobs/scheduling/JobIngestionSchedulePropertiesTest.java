package io.github.saveriobutright.jobradar.jobs.scheduling;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobIngestionSchedulePropertiesTest {

    @Test
    void rejectsInvalidScheduleValues() {
        assertThatThrownBy(() ->
                new JobIngestionScheduleProperties(
                        true,
                        0,
                        Duration.ofHours(1),
                        Duration.ZERO
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("page must be at least 1");

        assertThatThrownBy(() ->
                new JobIngestionScheduleProperties(
                        true,
                        1,
                        Duration.ZERO,
                        Duration.ZERO
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fixedDelay must be positive");

        assertThatThrownBy(() ->
                new JobIngestionScheduleProperties(
                        true,
                        1,
                        Duration.ofHours(1),
                        Duration.ofSeconds(-1)
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "initialDelay must not be negative"
                );
    }
}
