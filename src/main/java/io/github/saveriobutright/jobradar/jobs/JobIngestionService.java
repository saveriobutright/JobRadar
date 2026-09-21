package io.github.saveriobutright.jobradar.jobs;

import io.github.saveriobutright.jobradar.jobs.persistence.JobPostingStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class JobIngestionService {

    private final JobSearchService jobSearchService;
    private final JobPostingStore jobPostingStore;

    public JobIngestionService(
            JobSearchService jobSearchService,
            JobPostingStore jobPostingStore
    ) {
        this.jobSearchService = jobSearchService;
        this.jobPostingStore = jobPostingStore;
    }

    public int ingestPage(int pageNumber) {
        List<JobPosting> jobs = jobSearchService.findJobs(pageNumber);

        return jobPostingStore.upsertAll(jobs);
    }
}