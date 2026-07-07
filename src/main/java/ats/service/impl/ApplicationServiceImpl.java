package ats.service.impl;

import ats.constant.JobStatus;
import ats.dto.application.ApplyResponse;
import ats.dto.application.ApplyUploadRequest;
import ats.entity.Application;
import ats.entity.Candidate;
import ats.entity.Cv;
import ats.entity.Job;
import ats.entity.PipelineStage;
import ats.exception.BadRequestException;
import ats.exception.NotFoundException;
import ats.repository.ApplicationRepository;
import ats.repository.CandidateRepository;
import ats.repository.CvRepository;
import ats.repository.JobRepository;
import ats.repository.PipelineStageRepository;
import ats.service.ApplicationService;
import ats.storage.MinioStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L; // 10 MB

    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final CvRepository cvRepository;
    private final ApplicationRepository applicationRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final MinioStorage minioStorage;

    @Override
    @Transactional
    public ApplyResponse applyUpload(Long jobId, ApplyUploadRequest req, MultipartFile file) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Job not found with id: " + jobId));

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new BadRequestException("Job is not open for applications: " + jobId);
        }

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("validation.cvFile.empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("validation.cvFile.tooLarge");
        }

        Candidate candidate = resolveCandidate(req);

        MinioStorage.StoredResult stored;
        try {
            stored = minioStorage.uploadFromMultipart(file, "cv");
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded CV", e);
        }

        Cv cv = Cv.builder()
                .candidateId(candidate)
                .filePath(stored.storedKey())
                .fileName(stored.fileName())
                .fileType(stored.contentType())
                .build();
        // base
        cv = cvRepository.save(cv);

        PipelineStage firstStage = pipelineStageRepository
                .findAllByOrderByStageOrderAsc()
                .stream()
                .filter(s -> s.getStageOrder() != null)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No pipeline stage configured"));

        Application application = Application.builder()
                .jobId(job)
                .candidateId(candidate)
                .cvId(cv)
                .pipelineStageId(firstStage)
                .build();
        application = applicationRepository.save(application);

        log.info("created application id={} for job={} candidate={} cv={}",
                application.getId(), jobId, candidate.getId(), cv.getId());

        return new ApplyResponse(
                application.getId(),
                cv.getId(),
                cv.getFileName(),
                stored.storedKey(),
                stored.contentType());
    }

    private Candidate resolveCandidate(ApplyUploadRequest req) {
        Candidate existing = candidateRepository.findByEmail(req.getEmail());
        if (existing != null) {
            boolean phoneChanged = existing.getPhone() == null
                    && req.getPhone() != null
                    && !req.getPhone().isBlank();
            if (phoneChanged) {
                existing.setPhone(req.getPhone());
                return candidateRepository.save(existing);
            }
            return existing;
        }
        return candidateRepository.save(Candidate.builder()
                .name(req.getFullName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .build());
    }
}
