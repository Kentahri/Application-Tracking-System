package ats.service.impl;

import ats.dto.candidateapplication.CandidateApplicationResponse;
import ats.entity.*;
import ats.exception.BadRequestException;
import ats.exception.NotFoundException;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import ats.repository.ApplicationRepository;
import ats.repository.InterviewRepository;
import ats.repository.StageTransitionRepository;
import ats.service.CandidateApplicationService;
import ats.service.CandidateCvAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class CandidateApplicationServiceImpl implements CandidateApplicationService {
    private final ApplicationRepository applicationRepository;
    private final StageTransitionRepository stageTransitionRepository;
    private final InterviewRepository interviewRepository;
    private final CandidateCvAccessService candidateCvAccessService;

    @Override
    public PageResponse<CandidateApplicationResponse> getMyApplications(Principal principal, PagingRequest pagingRequest, String stage, String keyword) {
        Candidate candidate = candidateCvAccessService.getCurrentCandidate(principal);
        if (pagingRequest.getSize() > 50) throw new BadRequestException("Page size must not exceed 50");
        Page<Application> page = applicationRepository.findCandidateHistory(candidate.getId(), normalize(stage), normalize(keyword),
                pagingRequest.toPageable(Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))));
        return PageResponse.of(page.map(application -> toResponse(application, null, null,
                interviewRepository.existsByApplicationId_Id(application.getId()))));
    }

    @Override
    public CandidateApplicationResponse getMyApplicationDetail(Long applicationId, Principal principal) {
        Candidate candidate = candidateCvAccessService.getCurrentCandidate(principal);
        Application application = applicationRepository.findCandidateApplicationDetail(applicationId, candidate.getId())
                .orElseThrow(() -> new NotFoundException("Application not found"));
        List<StageTransition> history = stageTransitionRepository.findByApplicationIdWithStages(applicationId);
        Interview interview = interviewRepository.findByApplicationId_Id(applicationId).orElse(null);
        return toResponse(application, history, interview, interview != null);
    }

    private CandidateApplicationResponse toResponse(Application app, List<StageTransition> history, Interview interview, boolean hasInterview) {
        Job job = app.getJobId(); Cv cv = app.getCvId();
        var jobInfo = job == null ? null : new CandidateApplicationResponse.JobInfo(job.getId(), job.getTitle(), job.getLocation(), job.getSalaryMin(), job.getSalaryMax(), job.getStatus());
        var cvInfo = cv == null ? null : new CandidateApplicationResponse.CvInfo(cv.getId(), cv.getFileName(), cv.getFileType());
        var historyInfo = history == null ? null : history.stream().map(item -> new CandidateApplicationResponse.StageHistoryInfo(item.getId(), stage(item.getFromStageId()), stage(item.getToStageId()), item.getNotes(), item.getMovedAt())).toList();
        var interviewInfo = interview == null ? null : new CandidateApplicationResponse.InterviewInfo(interview.getId(), interview.getScheduledAt(), interview.getDurationMinutes(), interview.getMeetingLink(), interview.getStatus(), interview.getResult(), interview.getFeedBack());
        return new CandidateApplicationResponse(app.getId(), app.getCreatedAt(), app.getUpdatedAt(), jobInfo, stage(app.getPipelineStageId()), cvInfo, hasInterview, historyInfo, interviewInfo);
    }
    private CandidateApplicationResponse.StageInfo stage(PipelineStage value) { return value == null ? null : new CandidateApplicationResponse.StageInfo(value.getId(), value.getStageName(), value.getStageOrder()); }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
