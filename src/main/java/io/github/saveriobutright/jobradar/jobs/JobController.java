package io.github.saveriobutright.jobradar.jobs;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public final class JobController {

    private final StoredJobSearchService storedJobSearchService;

    public JobController(
            StoredJobSearchService storedJobSearchService
    ) {
        this.storedJobSearchService = storedJobSearchService;
    }

    @GetMapping
    public JobSearchResult searchJobs(
            @RequestParam(name = "page", defaultValue = "1")
            int page,
            @RequestParam(name = "size", defaultValue = "20")
            int size,
            @RequestParam(name = "query", required = false)
            String query,
            @RequestParam(name = "location", required = false)
            String location,
            @RequestParam(name = "remote", required = false)
            Boolean remote
    ) {
        JobSearchCriteria criteria = new JobSearchCriteria(
                page,
                size,
                query,
                location,
                remote
        );

        return storedJobSearchService.search(criteria);
    }
}
