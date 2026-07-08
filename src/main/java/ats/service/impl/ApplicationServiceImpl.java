package ats.service.impl;

import ats.constant.ApplicationReviewDecision;
import ats.constant.InterviewResult;
import ats.constant.InterviewStatus;
import ats.constant.JobStatus;
import ats.constant.UserRole;
import ats.constant.UserStatus;
import ats.dto.application.ApplicationDetailResponse;
import ats.dto.application.ApplicationReviewRequest;
import ats.dto.application.MoveApplicationStageRequest;
import ats.dto.application.MoveApplicationStageResponse;
import ats.dto.interview.CreateInterviewRequest;
import ats.dto.interview.InterviewScheduleResponse;
import ats.entity.Application;
import ats.entity.BaseEntity;
import ats.entity.Interview;
import ats.entity.Job;
import ats.entity.PipelineStage;
import ats.entity.StageTransition;
import ats.entity.User;
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
import ats.mapper.InterviewMapper;
import ats.repository.ApplicationRepository;
import ats.repository.InterviewRepository;
import ats.repository.PipelineStageRepository;
import ats.repository.StageTransitionRepository;
import ats.repository.UserRepository;
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

    private static final String STAGE_APPLIED = "Applied";
    private static final String STAGE_INTERVIEW = "Interview";
    private static final String STAGE_OFFER = "Offer";
    private static final String STAGE_REJECTED = "Rejected";
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L; // 10 MB

    private final ApplicationRepository applicationRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final StageTransitionRepository stageTransitionRepository;
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final ApplicationMapper applicationMapper;
    private final InterviewMapper interviewMapper;
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

    private PipelineStage getPipelineStageByNameOrThrow(String stageName) {
        PipelineStage stage = pipelineStageRepository.findByStageName(stageName);
        if (stage == null) {
            log.warn("Pipeline stage not found with name: {}", stageName);
            throw new NotFoundException(message("error.pipelineStage.name.notFound", stageName));
        }
        return stage;
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

    private void validateCurrentStage(Application application, String expectedStageName) {
        PipelineStage currentStage = application.getPipelineStageId();
        if (currentStage == null || !expectedStageName.equals(currentStage.getStageName())) {
            log.warn(
                    "Application id: {} expected current stage: {}, actual stage: {}",
                    application.getId(),
                    expectedStageName,
                    currentStage != null ? currentStage.getStageName() : null
            );
            throw new BadRequestException(message("error.application.stage.invalid", expectedStageName));
        }
    }

    private void validateStageTransition(Application application, PipelineStage fromStage, PipelineStage toStage) {
        String fromStageName = fromStage != null ? fromStage.getStageName() : null;
        String toStageName = toStage != null ? toStage.getStageName() : null;

        boolean allowed =
                (STAGE_APPLIED.equals(fromStageName)
                        && (STAGE_INTERVIEW.equals(toStageName) || STAGE_REJECTED.equals(toStageName)))
                        || (STAGE_INTERVIEW.equals(fromStageName)
                        && (STAGE_OFFER.equals(toStageName) || STAGE_REJECTED.equals(toStageName)));

        if (!allowed) {
            log.warn(
                    "Invalid stage transition for application id: {} from stage: {} to stage: {}",
                    application.getId(),
                    fromStageName,
                    toStageName
            );
            throw new BadRequestException(message("error.application.stage.transition.invalid", fromStageName, toStageName));
        }
    }

    private User getInterviewerOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Interviewer not found with id: {}", id);
                    return new NotFoundException(message("error.user.notFound", id));
                });
    }

    private void validateInterviewer(User interviewer) {
        if (interviewer.getRole() != UserRole.INTERVIEWER) {
            log.warn("User id: {} is not an interviewer", interviewer.getId());
            throw new BadRequestException(message("error.interview.interviewer.invalid"));
        }
        if (interviewer.getStatus() != UserStatus.ACTIVE) {
            log.warn("Interviewer id: {} is inactive", interviewer.getId());
            throw new BadRequestException(message("error.interview.interviewer.inactive"));
        }
    }

    private void validateInterviewerBelongsToJobDepartment(User interviewer, Job job) {
        Long interviewerDepartmentId = interviewer.getDepartmentId() != null ? interviewer.getDepartmentId().getId() : null;
        Long jobDepartmentId = job != null && job.getDepartmentId() != null ? job.getDepartmentId().getId() : null;

        if (!Objects.equals(interviewerDepartmentId, jobDepartmentId)) {
            log.warn(
                    "Interviewer id: {} department id: {} does not match job id: {} department id: {}",
                    interviewer.getId(),
                    interviewerDepartmentId,
                    job != null ? job.getId() : null,
                    jobDepartmentId
            );
            throw new BadRequestException(message("error.interview.interviewer.department.invalid"));
        }
    }

    private Interview getInterviewForApplicationOrThrow(Long applicationId, Long interviewId) {
        return interviewRepository.findByIdAndApplicationIdWithDetails(interviewId, applicationId)
                .orElseThrow(() -> {
                    log.warn("Interview id: {} not found for application id: {}", interviewId, applicationId);
                    return new NotFoundException(message("error.interview.notFound", interviewId));
                });
    }

    private void validateInterviewIsEditable(Interview interview) {
        if (interview.getStatus() != InterviewStatus.SCHEDULED) {
            log.warn("Interview id: {} is not editable because status is: {}", interview.getId(), interview.getStatus());
            throw new BadRequestException(message("error.interview.notEditable"));
        }
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private StageTransition createTransition(Application application,
                                             PipelineStage fromStage,
                                             PipelineStage toStage,
                                             String notes,
                                             LocalDateTime movedAt) {
        StageTransition transition = new StageTransition();
        initBase(transition, movedAt);
        transition.setApplicationId(application);
        transition.setFromStageId(fromStage);
        transition.setToStageId(toStage);
        transition.setMovedAt(movedAt);
        transition.setNotes(normalizeText(notes));
        return stageTransitionRepository.save(transition);
    }

    private void initBase(BaseEntity entity, LocalDateTime now) {
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setIsDeleted(false);
    }

    private void touch(BaseEntity entity, LocalDateTime now) {
        entity.setUpdatedAt(now);
    }

    @Override
    public ApplicationDetailResponse getDetail(Long applicationId, Principal principal) {
        log.debug("getting application detail for id: {}", applicationId);

        User recruiter = getRecruiterFromPrincipal(principal);
        Application application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> {
                    log.warn("Application not found with id: {}", applicationId);
                    return new NotFoundException(message("error.application.notFound", applicationId));
                });

        validateRecruiterOwnsApplicationJob(application, recruiter);
        return applicationMapper.toDetailResponse(
                application,
                stageTransitionRepository.findByApplicationIdWithStages(applicationId),
                interviewRepository.findByApplicationIdWithInterviewer(applicationId).orElse(null)
        );
    }

    @Override
    @Transactional
    public MoveApplicationStageResponse review(Long applicationId,
                                               ApplicationReviewRequest request,
                                               Principal principal) {
        log.info("reviewing application id: {} with decision: {}", applicationId, request.getDecision());

        User recruiter = getRecruiterFromPrincipal(principal);
        Application application = getApplicationOrThrow(applicationId);

        validateRecruiterOwnsApplicationJob(application, recruiter);
        validateJobIsMovable(application);
        validateCurrentStage(application, STAGE_APPLIED);

        PipelineStage fromStage = application.getPipelineStageId();
        PipelineStage toStage = request.getDecision() == ApplicationReviewDecision.PASS
                ? getPipelineStageByNameOrThrow(STAGE_INTERVIEW)
                : getPipelineStageByNameOrThrow(STAGE_REJECTED);

        LocalDateTime movedAt = LocalDateTime.now();
        application.setPipelineStageId(toStage);
        touch(application, movedAt);
        createTransition(application, fromStage, toStage, request.getNotes(), movedAt);

        log.info(
                "reviewed application id: {} from stage: {} to stage: {}",
                applicationId,
                fromStage.getStageName(),
                toStage.getStageName()
        );
        return applicationMapper.toMoveStageResponse(application, fromStage, toStage, movedAt);
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
        validateStageTransition(application, fromStage, toStage);

        LocalDateTime movedAt = LocalDateTime.now();
        application.setPipelineStageId(toStage);
        touch(application, movedAt);

        createTransition(application, fromStage, toStage, null, movedAt);

        log.info("moved application id: {} from stage id: {} to stage id: {}",
                applicationId,
                fromStage != null ? fromStage.getId() : null,
                toStage.getId());
        return applicationMapper.toMoveStageResponse(application, fromStage, toStage, movedAt);
    }

    @Override
    @Transactional
    public InterviewScheduleResponse createInterview(Long applicationId,
                                                     CreateInterviewRequest request,
                                                     Principal principal) {
        log.info("creating interview for application id: {} with interviewer id: {}",
                applicationId,
                request.getInterviewerId());

        User recruiter = getRecruiterFromPrincipal(principal);
        Application application = getApplicationOrThrow(applicationId);

        validateRecruiterOwnsApplicationJob(application, recruiter);
        validateJobIsMovable(application);
        validateCurrentStage(application, STAGE_INTERVIEW);

        if (interviewRepository.existsByApplicationId_Id(applicationId)) {
            log.warn("Interview already exists for application id: {}", applicationId);
            throw new BadRequestException(message("error.interview.application.exists"));
        }

        User interviewer = getInterviewerOrThrow(request.getInterviewerId());
        validateInterviewer(interviewer);
        validateInterviewerBelongsToJobDepartment(interviewer, application.getJobId());

        LocalDateTime now = LocalDateTime.now();
        Interview interview = new Interview();
        initBase(interview, now);
        interview.setApplicationId(application);
        interview.setInterviewerId(interviewer);
        interview.setScheduledAt(request.getScheduledAt());
        interview.setDurationMinutes(request.getDurationMinutes());
        interview.setMeetingLink(normalizeText(request.getMeetingLink()));
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setResult(InterviewResult.PENDING);
        interview.setFeedBack(normalizeText(request.getNote()));

        Interview savedInterview = interviewRepository.save(interview);
        log.info("created interview id: {} for application id: {}", savedInterview.getId(), applicationId);
        return interviewMapper.toScheduleResponse(savedInterview);
    }

    @Override
    @Transactional
    public InterviewScheduleResponse updateInterview(Long applicationId,
                                                     Long interviewId,
                                                     CreateInterviewRequest request,
                                                     Principal principal) {
        log.info("updating interview id: {} for application id: {}", interviewId, applicationId);

        User recruiter = getRecruiterFromPrincipal(principal);
        Application application = getApplicationOrThrow(applicationId);

        validateRecruiterOwnsApplicationJob(application, recruiter);
        validateJobIsMovable(application);
        validateCurrentStage(application, STAGE_INTERVIEW);

        Interview interview = getInterviewForApplicationOrThrow(applicationId, interviewId);
        validateInterviewIsEditable(interview);

        User interviewer = getInterviewerOrThrow(request.getInterviewerId());
        validateInterviewer(interviewer);
        validateInterviewerBelongsToJobDepartment(interviewer, application.getJobId());

        interview.setInterviewerId(interviewer);
        interview.setScheduledAt(request.getScheduledAt());
        interview.setDurationMinutes(request.getDurationMinutes());
        interview.setMeetingLink(normalizeText(request.getMeetingLink()));
        interview.setFeedBack(normalizeText(request.getNote()));
        interview.setUpdatedAt(LocalDateTime.now());

        Interview savedInterview = interviewRepository.save(interview);
        log.info("updated interview id: {} for application id: {}", interviewId, applicationId);
        return interviewMapper.toScheduleResponse(savedInterview);
    }
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
