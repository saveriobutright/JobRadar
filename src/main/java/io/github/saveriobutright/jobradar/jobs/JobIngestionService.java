package io.github.saveriobutright.jobradar.jobs;

import io.github.saveriobutright.jobradar.jobs.persistence.JobPostingStore;
import io.github.saveriobutright.jobradar.jobs.scoring.JobRelevanceProfile;
import io.github.saveriobutright.jobradar.jobs.scoring.JobRelevanceScorer;
import io.github.saveriobutright.jobradar.jobs.scoring.ScoredJobPosting;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class JobIngestionService {

    private final JobSearchService jobSearchService;
    private final JobPostingStore jobPostingStore;
    private final JobRelevanceScorer relevanceScorer;
    private final JobRelevanceProfile relevanceProfile;

    public JobIngestionService(
            JobSearchService jobSearchService,
            JobPostingStore jobPostingStore,
            JobRelevanceScorer relevanceScorer,
            JobRelevanceProfile relevanceProfile
    ) {
        this.jobSearchService = jobSearchService;
        this.jobPostingStore = jobPostingStore;
        this.relevanceScorer = relevanceScorer;
        this.relevanceProfile = relevanceProfile;
    }

    public int ingestPage(int pageNumber) {
        List<JobPosting> jobs =
                jobSearchService.findJobs(pageNumber);

        List<ScoredJobPosting> scoredJobs = jobs.stream()
                .map(job -> new ScoredJobPosting(
                        job,
                        relevanceScorer.score(
                                job,
                                relevanceProfile
                        )
                ))
                .toList();

        return jobPostingStore.upsertAllScored(
                scoredJobs
        );
    }
}
