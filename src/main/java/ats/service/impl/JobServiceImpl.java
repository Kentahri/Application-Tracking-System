package ats.service.impl;

import ats.constant.JobStatus;
import ats.dto.kanban.KanbanApplicationResponse;
import ats.dto.kanban.KanbanBoardResponse;
import ats.dto.kanban.KanbanStageResponse;
import ats.dto.job.JobRequest;
import ats.dto.job.JobResponse;
import ats.dto.job.JobUpdateRequest;
import ats.entity.Application;
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
import ats.repository.JobRepository;
import ats.repository.PipelineStageRepository;
import ats.repository.UserRepository;
import ats.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
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

    private final JobRepository jobRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final ApplicationRepository applicationRepository;
    private final JobMapper jobMapper;
    private final KanbanMapper kanbanMapper;

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

    @Override
    public PageResponse<JobResponse> getAllJobs(Principal principal, PagingRequest pagingRequest) {
        log.debug("getting recruiter jobs page: {}, size: {}", pagingRequest.getPage(), pagingRequest.getSize());

        User recruiter = getRecruiterFromPrincipal(principal);
        Page<Job> jobs = jobRepository.findByRecruiterId_Id(
                recruiter.getId(),
                pagingRequest.toPageable(Sort.by(Sort.Direction.DESC, "id"))
        );
        Page<JobResponse> responses = jobs.map(jobMapper::toDto);
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
        return jobMapper.toDto(job);
    }

    @Override
    public KanbanBoardResponse getKanbanBoard(Long jobId, Principal principal) {
        log.debug("getting Kanban board for job id: {}", jobId);

        User recruiter = getRecruiterFromPrincipal(principal);
        Job job = getJobOrThrow(jobId);
        validateRecruiterOwnsJob(job, recruiter);

        List<PipelineStage> stages = pipelineStageRepository.findAllByOrderByStageOrderAsc();
        List<Application> applications = applicationRepository.findByJobIdWithDetails(jobId);
        Map<Long, List<Application>> applicationsByStageId = applications.stream()
                .collect(Collectors.groupingBy(application -> application.getPipelineStageId().getId()));

        List<KanbanStageResponse> stageResponses = stages.stream()
                .map(stage -> {
                    List<KanbanApplicationResponse> applicationResponses = applicationsByStageId
                            .getOrDefault(stage.getId(), List.of())
                            .stream()
                            .map(kanbanMapper::toApplicationResponse)
                            .toList();

                    return kanbanMapper.toStageResponse(stage, applicationResponses);
                })
                .toList();

        return kanbanMapper.toBoardResponse(job, stageResponses, applications.size());
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
        User recruiter = getRecruiterFromPrincipal(principal);

        request.setTitle(title);
        request.setStatus(normalizeStatus(request.getStatus()));

        Job job = jobMapper.toEntity(request);
        job.setRecruiterId(recruiter);
        Job saved = jobRepository.save(job);

        log.info("created job with id: {}", saved.getId());
        return jobMapper.toDto(saved);
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

        jobMapper.updateEntity(request, job);

        log.info("updated job id: {} with data: {}", id, request);
        return jobMapper.toDto(job);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("deleting job with id: {}", id);

        Job job = getJobOrThrow(id);
        jobRepository.delete(job);
        log.info("deleted job with id: {}", id);
    }
}
