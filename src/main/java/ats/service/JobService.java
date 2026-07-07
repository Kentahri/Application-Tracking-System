package ats.service;

import ats.dto.kanban.KanbanBoardResponse;
import ats.constant.JobStatus;
import ats.dto.job.JobRequest;
import ats.dto.job.JobResponse;
import ats.dto.job.JobUpdateRequest;
import ats.http.PageResponse;
import ats.http.PagingRequest;

import java.security.Principal;

public interface JobService {

    PageResponse<JobResponse> getAllJobs(Principal principal, PagingRequest pagingRequest, JobStatus status);

    JobResponse getJobById(Long id);

    KanbanBoardResponse getKanbanBoard(Long jobId, Principal principal);

    JobResponse create(JobRequest request, Principal principal);

    JobResponse update(Long id, JobUpdateRequest request);

    void delete(Long id);
}

