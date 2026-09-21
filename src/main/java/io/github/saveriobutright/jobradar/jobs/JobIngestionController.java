package io.github.saveriobutright.jobradar.jobs;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingestions")
public final class JobIngestionController {

    private final JobIngestionService jobIngestionService;

    public JobIngestionController(
            JobIngestionService jobIngestionService
    ) {
        this.jobIngestionService = jobIngestionService;
    }

    @PostMapping("/jobs")
    public JobIngestionResult ingestJobs(
            @RequestParam(name = "page", defaultValue = "1")
            int page
    ) {
        int processedJobs = jobIngestionService.ingestPage(page);

        return new JobIngestionResult(page, processedJobs);
    }
}