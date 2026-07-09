package ats.service.impl;

import ats.constant.JobStatus;
import ats.constant.UserRole;
import ats.constant.UserStatus;
import ats.dto.interview.InterviewerResponse;
import ats.dto.kanban.KanbanApplicationResponse;
import ats.dto.kanban.KanbanBoardResponse;
import ats.dto.kanban.KanbanStageResponse;
import ats.dto.job.JobRequest;
import ats.dto.job.JobResponse;
import ats.dto.job.JobUpdateRequest;
import ats.entity.Application;
import ats.entity.Interview;
import ats.entity.Job;
import ats.entity.PipelineStage;
import ats.entity.User;
import ats.exception.BadRequestException;
import ats.exception.NotFoundException;
import ats.exception.UnauthorizedException;
import ats.helper.MessageHelper;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import ats.mapper.JobMapper;
import ats.mapper.KanbanMapper;
import ats.repository.ApplicationRepository;
import ats.repository.DepartmentRepository;
import ats.repository.InterviewRepository;
import ats.repository.JobRepository;
import ats.repository.PipelineStageRepository;
import ats.repository.projection.JobWithApplicationCountProjection;
import ats.repository.UserRepository;
import ats.service.JobService;
import ats.service.JobVectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private static final JobStatus DEFAULT_STATUS = JobStatus.DRAFT;
    private static final List<String> KANBAN_STAGE_NAMES = List.of("Applied", "Interview", "Offer", "Rejected");

    private final JobRepository jobRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final JobMapper jobMapper;
    private final KanbanMapper kanbanMapper;
    private final JobVectorService jobVectorService;

    private String message(String code, Object... args) {
        return MessageHelper.getMessage(code, args);
    }

    private Job getJobOrThrow(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Job not found with id: {}", id);
                    return new NotFoundException(message("error.job.notFound", id));
                });
    }

    private void validateDepartmentExists(Long departmentId) {
        if (departmentId != null && !departmentRepository.existsById(departmentId)) {
            log.warn("Department not found with id: {}", departmentId);
            throw new NotFoundException(message("error.department.notFound", departmentId));
        }
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

    private void validateSalaryRange(BigDecimal salaryMin, BigDecimal salaryMax) {
        if (salaryMin != null && salaryMax != null && salaryMin.compareTo(salaryMax) > 0) {
            log.warn("Invalid salary range: salaryMin={} salaryMax={}", salaryMin, salaryMax);
            throw new BadRequestException(message("error.job.salaryRange.invalid"));
        }
    }

    private void validateDeadline(LocalDate deadline) {
        if (deadline == null || deadline.isBefore(LocalDate.now())) {
            log.warn("Invalid job deadline: {}", deadline);
            throw new BadRequestException(message("error.job.deadline.invalid"));
        }
    }

    private JobStatus normalizeStatus(JobStatus status) {
        if (status == null) {
            return DEFAULT_STATUS;
        }
        return status;
    }

    private void validateRecruiterOwnsJob(Job job, User recruiter) {
        Long jobRecruiterId = job.getRecruiterId() != null ? job.getRecruiterId().getId() : null;
        if (!Objects.equals(jobRecruiterId, recruiter.getId())) {
            log.warn("Recruiter id: {} attempted to access job id: {}", recruiter.getId(), job.getId());
            throw new UnauthorizedException(message("error.job.accessDenied"));
        }
    }

    private JobResponse toJobResponse(Job job, Long applicationCount) {
        JobResponse response = jobMapper.toDto(job);
        response.setApplicationCount(applicationCount != null ? applicationCount : 0L);
        return response;
    }

    @Override
    public PageResponse<JobResponse> getAllJobs(Principal principal, PagingRequest pagingRequest, JobStatus status) {
        log.debug("getting recruiter jobs page: {}, size: {}, status: {}",
                pagingRequest.getPage(),
                pagingRequest.getSize(),
                status);

        User recruiter = getRecruiterFromPrincipal(principal);
        Page<JobWithApplicationCountProjection> jobs = jobRepository.findByRecruiterIdWithApplicationCount(
                recruiter.getId(),
                status,
                pagingRequest.toPageable(Sort.by(Sort.Direction.DESC, "id"))
        );
        Page<JobResponse> responses = jobs.map(job -> toJobResponse(job.getJob(), job.getApplicationCount()));
        return PageResponse.of(responses);
    }

    @Override
    public PageResponse<JobResponse> getAllPostedJobs(PagingRequest pagingRequest) {
        log.debug("getting all posted jobs page: {}, size: {}", pagingRequest.getPage(), pagingRequest.getSize());

        Page<Job> jobs = jobRepository.findByStatusWithDepartment(
                JobStatus.PUBLISHED,
                pagingRequest.toPageable(Sort.by(Sort.Direction.DESC, "id"))
        );
        Page<JobResponse> responses = jobs.map(jobMapper::toDto);
        return PageResponse.of(responses);
    }

    @Override
    public JobResponse getJobById(Long id) {
        log.debug("getting job by id: {}", id);

        Job job = getJobOrThrow(id);
        return toJobResponse(job, applicationRepository.countByJobId_Id(id));
    }

    @Override
    public KanbanBoardResponse getKanbanBoard(Long jobId, Principal principal) {
        log.debug("getting Kanban board for job id: {}", jobId);

        User recruiter = getRecruiterFromPrincipal(principal);
        Job job = getJobOrThrow(jobId);
        validateRecruiterOwnsJob(job, recruiter);

        List<PipelineStage> stages = pipelineStageRepository.findAllByStageNameInOrderByStageOrderAsc(KANBAN_STAGE_NAMES);
        List<Application> applications = applicationRepository.findByJobIdWithDetails(jobId);
        Map<Long, Interview> interviewsByApplicationId = interviewRepository.findByJobIdWithInterviewer(jobId)
                .stream()
                .collect(Collectors.toMap(
                        interview -> interview.getApplicationId().getId(),
                        interview -> interview,
                        (first, second) -> first
                ));
        Map<Long, List<Application>> applicationsByStageId = applications.stream()
                .collect(Collectors.groupingBy(application -> application.getPipelineStageId().getId()));

        List<KanbanStageResponse> stageResponses = stages.stream()
                .map(stage -> {
                    List<KanbanApplicationResponse> applicationResponses = applicationsByStageId
                            .getOrDefault(stage.getId(), List.of())
                            .stream()
                            .map(application -> kanbanMapper.toApplicationResponse(
                                    application,
                                    interviewsByApplicationId.get(application.getId())
                            ))
                            .toList();

                    return kanbanMapper.toStageResponse(stage, applicationResponses);
                })
                .toList();

        return kanbanMapper.toBoardResponse(job, stageResponses, applications.size());
    }

    @Override
    public List<InterviewerResponse> getInterviewersByJobDepartment(Long jobId, Principal principal) {
        log.debug("getting interviewers for job id: {}", jobId);

        User recruiter = getRecruiterFromPrincipal(principal);
        Job job = getJobOrThrow(jobId);
        validateRecruiterOwnsJob(job, recruiter);

        Long departmentId = job.getDepartmentId() != null ? job.getDepartmentId().getId() : null;
        if (departmentId == null) {
            return List.of();
        }

        return userRepository
                .findByRoleAndStatusAndDepartmentId_IdOrderByNameAsc(UserRole.INTERVIEWER, UserStatus.ACTIVE, departmentId)
                .stream()
                .map(interviewer -> new InterviewerResponse(
                        interviewer.getId(),
                        interviewer.getName(),
                        interviewer.getEmail(),
                        interviewer.getDepartmentId() != null ? interviewer.getDepartmentId().getId() : null
                ))
                .toList();
    }

    @Override
    @Transactional
    public JobResponse create(JobRequest request, Principal principal) {
        log.info("creating new job with title: {}", request.getTitle());

        String title = request.getTitle().trim();

        if (jobRepository.existsByTitle(title)) {
            log.warn("Job title already exists: {}", title);
            throw new BadRequestException(message("error.job.title.exists"));
        }

        validateDepartmentExists(request.getDepartmentId());
        validateSalaryRange(request.getSalaryMin(), request.getSalaryMax());
        validateDeadline(request.getDeadline());
        User recruiter = getRecruiterFromPrincipal(principal);

        request.setTitle(title);
        request.setStatus(normalizeStatus(request.getStatus()));

        Job job = jobMapper.toEntity(request);
        job.setIsDeleted(false);
        job.setRecruiterId(recruiter);
        Job saved = jobRepository.save(job);
        jobVectorService.upsert(saved.getId());

        log.info("created job with id: {}", saved.getId());
        return toJobResponse(saved, 0L);
    }

    @Override
    @Transactional
    public JobResponse update(Long id, JobUpdateRequest request) {
        log.info("updating job with id: {}", id);

        Job job = getJobOrThrow(id);

        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isBlank()) {
                throw new BadRequestException(message("error.job.title.blank"));
            }
            if (jobRepository.existsByTitleAndIdNot(title, id)) {
                log.warn("Job title already exists: {}", title);
                throw new BadRequestException(message("error.job.title.exists"));
            }
            request.setTitle(title);
        }

        validateDepartmentExists(request.getDepartmentId());

        BigDecimal salaryMin = request.getSalaryMin() != null ? request.getSalaryMin() : job.getSalaryMin();
        BigDecimal salaryMax = request.getSalaryMax() != null ? request.getSalaryMax() : job.getSalaryMax();
        validateSalaryRange(salaryMin, salaryMax);

        LocalDate deadline = request.getDeadline() != null ? request.getDeadline() : job.getDeadline();
        JobStatus status = request.getStatus() != null ? request.getStatus() : job.getStatus();
        if (request.getDeadline() != null || JobStatus.PUBLISHED.equals(status)) {
            validateDeadline(deadline);
        }

        jobMapper.updateEntity(request, job);
        job.setUpdatedAt(LocalDateTime.now());
        jobVectorService.upsert(id);
        log.info("updated job id: {} with data: {}", id, request);
        return toJobResponse(job, applicationRepository.countByJobId_Id(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("deleting job with id: {}", id);

        Job job = getJobOrThrow(id);
        job.setDeletedAt(LocalDateTime.now());
        jobRepository.delete(job);
        jobVectorService.delete(id);
        log.info("deleted job with id: {}", id);
    }
}
