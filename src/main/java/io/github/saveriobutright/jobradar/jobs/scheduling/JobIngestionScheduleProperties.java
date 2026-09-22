package io.github.saveriobutright.jobradar.jobs.scheduling;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Objects;

@ConfigurationProperties("jobradar.ingestion.schedule")
public record JobIngestionScheduleProperties(
        boolean enabled,
        int page,
        Duration fixedDelay,
        Duration initialDelay
) {

    public JobIngestionScheduleProperties {
        if (page < 1) {
            throw new IllegalArgumentException(
                    "page must be at least 1"
            );
        }

        Objects.requireNonNull(
                fixedDelay,
                "fixedDelay must not be null"
        );

        if (fixedDelay.isZero() || fixedDelay.isNegative()) {
            throw new IllegalArgumentException(
                    "fixedDelay must be positive"
            );
        }

        Objects.requireNonNull(
                initialDelay,
                "initialDelay must not be null"
        );

        if (initialDelay.isNegative()) {
            throw new IllegalArgumentException(
                    "initialDelay must not be negative"
            );
        }
    }
}
