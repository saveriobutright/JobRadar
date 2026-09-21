package io.github.saveriobutright.jobradar.jobs;

import io.github.saveriobutright.jobradar.sources.JobSource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class JobSearchService {

    private final List<JobSource> jobSources;

    public JobSearchService(List<JobSource> jobSources) {
        this.jobSources = List.copyOf(jobSources);
    }

    public List<JobPosting> findJobs(int pageNumber) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException(
                    "pageNumber must be at least 1"
            );
        }

        return jobSources.stream()
                .flatMap(source -> source.fetchPage(pageNumber).stream())
                .toList();
    }
}