package ats.controller;

import ats.dto.kanban.KanbanBoardResponse;
import ats.dto.job.JobRequest;
import ats.dto.job.JobResponse;
import ats.dto.job.JobUpdateRequest;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ats.service.JobService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/recruiter/jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recruiter Jobs", description = "APIs for recruiters to manage jobs")
public class JobController {

    private final JobService jobService;

    @GetMapping
    @Operation(summary = "Get recruiter jobs", description = "Get paginated jobs created by the authenticated recruiter")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jobs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public PageResponse<JobResponse> getAll(@Parameter(description = "Page number, starting from 1")
                                            @RequestParam(defaultValue = "1") int page,
                                            @Parameter(description = "Number of records per page")
                                            @RequestParam(defaultValue = "10") int size,
                                            Principal principal) {
        log.debug("REST request to get recruiter jobs page: {}, size: {}", page, size);
        return jobService.getAllJobs(principal, new PagingRequest(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by id", description = "Get job detail by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public JobResponse getById(@Parameter(description = "Job id") @PathVariable Long id) {
        log.debug("REST request to get job by id: {}", id);
        return jobService.getJobById(id);
    }

    @GetMapping("/{id}/kanban")
    @Operation(summary = "Get job Kanban board", description = "Get applications grouped by pipeline stages for a job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kanban board retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated or access denied"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public KanbanBoardResponse getKanbanBoard(@Parameter(description = "Job id") @PathVariable Long id,
                                              Principal principal) {
        log.debug("REST request to get Kanban board for job id: {}", id);
        return jobService.getKanbanBoard(id, principal);
    }

    @PostMapping
    @Operation(summary = "Create job", description = "Create a new job for the authenticated recruiter")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public JobResponse create(@Valid @RequestBody JobRequest request, Principal principal) {
        log.debug("REST request to create job: {}", request);
        return jobService.create(request, principal);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update job", description = "Update an existing job by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public JobResponse update(@Parameter(description = "Job id") @PathVariable Long id,
                              @Valid @RequestBody JobUpdateRequest request) {
        log.debug("REST request to update job id: {}", id);
        return jobService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete job", description = "Soft delete a job by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public void delete(@Parameter(description = "Job id") @PathVariable Long id) {
        log.debug("REST request to delete job: {}", id);
        jobService.delete(id);
    }
}
