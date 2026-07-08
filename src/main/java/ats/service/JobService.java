package ats.service;

import ats.dto.kanban.KanbanBoardResponse;
import ats.constant.JobStatus;
import ats.dto.interview.InterviewerResponse;
import ats.dto.job.JobRequest;
import ats.dto.job.JobResponse;
import ats.dto.job.JobUpdateRequest;
import ats.http.PageResponse;
import ats.http.PagingRequest;

import java.security.Principal;
import java.util.List;

public interface JobService {

    PageResponse<JobResponse> getAllJobs(Principal principal, PagingRequest pagingRequest, JobStatus status);

    PageResponse<JobResponse> getAllPostedJobs(PagingRequest pagingRequest);

    JobResponse getJobById(Long id);

    KanbanBoardResponse getKanbanBoard(Long jobId, Principal principal);

    List<InterviewerResponse> getInterviewersByJobDepartment(Long jobId, Principal principal);

    JobResponse create(JobRequest request, Principal principal);

    JobResponse update(Long id, JobUpdateRequest request);

    void delete(Long id);
}

