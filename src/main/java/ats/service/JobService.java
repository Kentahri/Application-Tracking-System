package ats.service;

import ats.dto.job.JobDeleteRequest;
import ats.dto.job.JobRequest;
import ats.dto.job.JobResponse;
import ats.dto.job.JobUpdateRequest;

import java.util.List;

public interface JobService {

    List<JobResponse> getAllJobs();

    JobResponse getJobById(Long id);

    JobResponse create(JobRequest request);

    JobResponse update(Long id, JobUpdateRequest request);

    void delete(JobDeleteRequest request);
}

