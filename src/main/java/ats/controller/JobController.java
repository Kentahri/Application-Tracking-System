package ats.controller;

import ats.dto.job.JobDeleteRequest;
import ats.dto.job.JobRequest;
import ats.dto.job.JobResponse;
import ats.dto.job.JobUpdateRequest;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;

    @GetMapping
    public List<JobResponse> getAll() {
        log.debug("REST request to get all jobs");
        return jobService.getAllJobs();
    }

    @GetMapping("/{id}")
    public JobResponse getById(@PathVariable Long id) {
        log.debug("REST request to get job by id: {}", id);
        return jobService.getJobById(id);
    }

    @PostMapping
    public JobResponse create(@RequestBody JobRequest request) {
        log.debug("REST request to create job: {}", request);
        return jobService.create(request);
    }

    @PutMapping("/{id}")
    public JobResponse update(@PathVariable Long id,
                                     @RequestBody JobUpdateRequest request) {
        log.debug("REST request to update job id: {}", id);
        return jobService.update(id, request);
    }

    @DeleteMapping
    public void delete(@RequestBody JobDeleteRequest request) {
        log.debug("REST request to delete job: {}", request.getId());
        jobService.delete(request);
    }
}

