package io.github.saveriobutright.jobradar.sources.arbeitnow;

import io.github.saveriobutright.jobradar.jobs.JobPosting;
import org.springframework.stereotype.Component;
import org.jsoup.Jsoup;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.List;

@Component
public final class ArbeitnowJobMapper {

    public JobPosting map(ArbeitnowJob job) {
        Objects.requireNonNull(job, "job must not be null");

        String sourceId = Objects.requireNonNull(
                job.slug(),
                "Arbeitnow slug must not be null"
        );

        URI sourceUrl = URI.create(Objects.requireNonNull(
                job.url(),
                "Arbeitnow URL must not be null"
        ));

        Instant postedAt = Instant.ofEpochSecond(Objects.requireNonNull(
                job.createdAt(),
                "Arbeitnow created_at must not be null"
        ));

        return new JobPosting(
                "Arbeitnow",
                sourceId,
                job.title(),
                job.companyName(),
                toPlainText(job.description()),
                normalizeValues(job.tags()),
                normalizeValues(job.jobTypes()),
                job.location(),
                Boolean.TRUE.equals(job.remote()),
                sourceUrl,
                postedAt
        );
    }

    private static String toPlainText(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }

        return Jsoup.parse(html).text();
    }

    private static List<String> normalizeValues(
            List<String> values
    ) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .toList();
    }
}
