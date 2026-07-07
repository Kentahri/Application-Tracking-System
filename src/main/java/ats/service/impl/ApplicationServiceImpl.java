package ats.service.impl;

import ats.constant.JobStatus;
import ats.dto.application.ApplyResponse;
import ats.dto.application.ApplyUploadRequest;
import ats.dto.application.MoveApplicationStageRequest;
import ats.dto.application.MoveApplicationStageResponse;
import ats.entity.*;
import ats.exception.BadRequestException;
import ats.exception.NotFoundException;
import ats.exception.UnauthorizedException;
import ats.helper.MessageHelper;
import ats.mapper.ApplicationMapper;
import ats.repository.*;
import ats.service.ApplicationService;
import ats.storage.MinioStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L; // 10 MB

    private final ApplicationRepository applicationRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final StageTransitionRepository stageTransitionRepository;
    private final UserRepository userRepository;
    private final ApplicationMapper applicationMapper;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final CvRepository cvRepository;
    private final MinioStorage minioStorage;

    private String message(String code, Object... args) {
        return MessageHelper.getMessage(code, args);
    }

    private User getRecruiterFromPrincipal(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new UnauthorizedException(message("error.auth.unauthorized"));
        }

        String email = principal.getName();
        User recruiter = userRepository.findByEmail(email);
        if (recruiter == null) {
            log.warn("Authenticated user not found with email: {}", email);
            throw new NotFoundException(message("error.user.email.notFound", email));
        }
        return recruiter;
    }

    private Application getApplicationOrThrow(Long id) {
        return applicationRepository.findByIdWithJobAndStage(id)
                .orElseThrow(() -> {
                    log.warn("Application not found with id: {}", id);
                    return new NotFoundException(message("error.application.notFound", id));
                });
    }

    private PipelineStage getPipelineStageOrThrow(Long id) {
        return pipelineStageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Pipeline stage not found with id: {}", id);
                    return new NotFoundException(message("error.pipelineStage.notFound", id));
                });
    }

    private void validateRecruiterOwnsApplicationJob(Application application, User recruiter) {
        Job job = application.getJobId();
        Long jobRecruiterId = job != null && job.getRecruiterId() != null ? job.getRecruiterId().getId() : null;
        if (!Objects.equals(jobRecruiterId, recruiter.getId())) {
            log.warn("Recruiter id: {} attempted to move application id: {}", recruiter.getId(), application.getId());
            throw new UnauthorizedException(message("error.job.accessDenied"));
        }
    }

    private void validateJobIsMovable(Application application) {
        Job job = application.getJobId();
        if (job != null && JobStatus.CLOSED.equals(job.getStatus())) {
            log.warn("Attempted to move application id: {} for closed job id: {}", application.getId(), job.getId());
            throw new BadRequestException(message("error.application.job.closed"));
        }
    }

    @Override
    @Transactional
    public MoveApplicationStageResponse moveStage(Long applicationId,
                                                  MoveApplicationStageRequest request,
                                                  Principal principal) {
        log.info("moving application id: {} to stage id: {}", applicationId, request.getToStageId());

        User recruiter = getRecruiterFromPrincipal(principal);
        Application application = getApplicationOrThrow(applicationId);
        PipelineStage fromStage = application.getPipelineStageId();
        PipelineStage toStage = getPipelineStageOrThrow(request.getToStageId());

        validateRecruiterOwnsApplicationJob(application, recruiter);
        validateJobIsMovable(application);

        if (fromStage != null && Objects.equals(fromStage.getId(), toStage.getId())) {
            log.warn("Application id: {} is already in stage id: {}", applicationId, toStage.getId());
            throw new BadRequestException(message("error.application.stage.same"));
        }

        LocalDateTime movedAt = LocalDateTime.now();
        application.setPipelineStageId(toStage);

        StageTransition transition = new StageTransition();
        transition.setApplicationId(application);
        transition.setFromStageId(fromStage);
        transition.setToStageId(toStage);
        transition.setMovedAt(movedAt);
        stageTransitionRepository.save(transition);

        log.info("moved application id: {} from stage id: {} to stage id: {}",
                applicationId,
                fromStage != null ? fromStage.getId() : null,
                toStage.getId());
        return applicationMapper.toMoveStageResponse(application, fromStage, toStage, movedAt);
    }

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
