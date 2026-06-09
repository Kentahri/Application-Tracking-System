package ats.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ats.dto.job.JobDeleteRequest;
import ats.dto.job.JobRequest;
import ats.dto.job.JobResponse;
import ats.dto.job.JobUpdateRequest;
import ats.entity.Job;
import ats.mapper.JobMapper;
import ats.repository.JobRepository;
import ats.service.JobService;

import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    private Job getJobOrThrow(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Job not found with id: {}", id);
                    return new RuntimeException("Không tìm thấy công việc với id: " + id);
                });
    }

    @Override
    public List<JobResponse> getAllJobs() {
        log.debug("getting all jobs");
        List<Job> jobs = jobRepository.findAll();
        List<JobResponse> responses = jobMapper.toDto(jobs);
        return responses;
    }

    @Override
    public JobResponse getJobById(Long id) {
        log.debug("getting job by id: {}", id);

        Job job = getJobOrThrow(id);
        JobResponse response = jobMapper.toDto(job);
        return response;
    }

    @Override
    @Transactional
    public JobResponse create(JobRequest request) {
        log.info("creating new job with title: {}", request.getTitle());

        if(jobRepository.existsByTitle(request.getTitle())) {
            log.warn("Job title already exists: {}", request.getTitle());
            throw new RuntimeException("Tiêu đề công việc đã tồn tại");
        }

        Job job = jobMapper.toEntity(request);
        Job saved = jobRepository.save(job);

        log.info("created job with id: {}", saved.getId());
        return jobMapper.toDto(saved);
    }

    @Override
    @Transactional
    public JobResponse update(Long id, JobUpdateRequest request) {
        log.info("updating job with id: {}", id);

        Job job = getJobOrThrow(id);

        jobMapper.updateEntity(request, job);

        log.info("updated job id: {} with data: {}", id, request);
        return jobMapper.toDto(job);
    }

    @Override
    @Transactional
    public void delete(JobDeleteRequest request) {
        log.info("deleting job with id: {}", request.getId());

        Job job = getJobOrThrow(request.getId());
        jobRepository.delete(job);
        log.info("deleted job with id: {}", request.getId());
    }
}

