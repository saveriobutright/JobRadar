package io.github.saveriobutright.jobradar.jobs;

import io.github.saveriobutright.jobradar.jobs.persistence.JobPostingStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class StoredJobSearchService {

    private final JobPostingStore jobPostingStore;

    public StoredJobSearchService(
            JobPostingStore jobPostingStore
    ) {
        this.jobPostingStore = jobPostingStore;
    }

    @Transactional(
            readOnly = true,
            isolation = Isolation.REPEATABLE_READ
    )
    public JobSearchResult search(
            JobSearchCriteria criteria
    ) {
        Objects.requireNonNull(
                criteria,
                "criteria must not be null"
        );

        long totalItems = jobPostingStore.count(criteria);
        List<StoredJobPosting> items =
                jobPostingStore.find(criteria);

        return JobSearchResult.from(
                items,
                criteria,
                totalItems
        );
    }
}
