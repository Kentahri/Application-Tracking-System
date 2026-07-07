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
import ats.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private static final String STAGE_REJECTED = "Rejected";

    private final ApplicationRepository applicationRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final StageTransitionRepository stageTransitionRepository;
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final ApplicationMapper applicationMapper;
    private final InterviewMapper interviewMapper;

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
}
