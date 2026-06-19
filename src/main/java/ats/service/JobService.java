package ats.service;

import ats.dto.job.JobRequest;
import ats.dto.job.JobResponse;
import ats.dto.job.JobUpdateRequest;
import ats.http.PageResponse;
import ats.http.PagingRequest;

import java.security.Principal;

public interface JobService {

    PageResponse<JobResponse> getAllJobs(Principal principal, PagingRequest pagingRequest);

    JobResponse getJobById(Long id);

    JobResponse create(JobRequest request, Principal principal);

    JobResponse update(Long id, JobUpdateRequest request);

    void delete(Long id);
}

