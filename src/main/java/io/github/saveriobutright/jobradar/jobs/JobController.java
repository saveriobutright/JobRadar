package io.github.saveriobutright.jobradar.jobs;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public final class JobController {

    private final JobSearchService jobSearchService;

    public JobController(JobSearchService jobSearchService) {
        this.jobSearchService = jobSearchService;
    }

    @GetMapping
    public List<JobPosting> getJobs(
            @RequestParam(name = "page", defaultValue = "1")
            int page
    ) {
        return jobSearchService.findJobs(page);
    }
}