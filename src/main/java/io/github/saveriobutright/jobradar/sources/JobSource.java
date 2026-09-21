package io.github.saveriobutright.jobradar.sources;

import io.github.saveriobutright.jobradar.jobs.JobPosting;

import java.util.List;

public interface JobSource {
    List<JobPosting> fetchPage(int pageNumber);
}
