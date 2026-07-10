package ats.service.impl;

import ats.entity.Job;
import ats.repository.JobRepository;
import ats.service.EmbeddingService;
import ats.service.JobVectorService;
import ats.service.QdrantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobVectorServiceImpl implements JobVectorService {

    private final JobRepository jobRepository;
    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;

    @Override
    @Async
    public void upsert(Long jobId) {
        try {
            Job job = jobRepository.findById(jobId).orElse(null);
            if (job == null) {
                log.warn("Skip syncing job vector because job id {} was not found", jobId);
                return;
            }

            String content = buildJobContent(job);
            List<Float> vector = embeddingService.embed(content);
            qdrantService.upsertJob(job, vector);

            log.info("Synced job vector for job id: {}", jobId);
        } catch (Exception e) {
            log.warn("Failed to sync job vector for job id: {}", jobId, e);
        }
    }

    @Override
    @Async
    public void delete(Long jobId) {
        try {
            qdrantService.deleteJob(jobId);
            log.info("Deleted job vector for job id: {}", jobId);
        } catch (Exception e) {
            log.warn("Failed to delete job vector for job id: {}", jobId, e);
        }
    }

    private String buildJobContent(Job job) {
        return """
                Title: %s
                Description: %s
                Location: %s
                Salary min: %s
                Salary max: %s
                Deadline: %s
                Status: %s
                Department: %s
                """.formatted(
                valueOrEmpty(job.getTitle()),
                valueOrEmpty(job.getDescription()),
                valueOrEmpty(job.getLocation()),
                job.getSalaryMin() != null ? job.getSalaryMin() : "",
                job.getSalaryMax() != null ? job.getSalaryMax() : "",
                job.getDeadline() != null ? job.getDeadline() : "",
                job.getStatus() != null ? job.getStatus().name() : "",
                job.getDepartmentId() != null
                        ? valueOrEmpty(job.getDepartmentId().getDepartmentName())
                        : ""
        );
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }
}
