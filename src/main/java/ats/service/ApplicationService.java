package ats.service;

import ats.dto.application.ApplicationDetailResponse;
import ats.dto.application.ApplicationReviewRequest;
import ats.dto.application.MoveApplicationStageRequest;
import ats.dto.application.MoveApplicationStageResponse;
import ats.dto.interview.CreateInterviewRequest;
import ats.dto.interview.InterviewScheduleResponse;

import java.security.Principal;
import ats.dto.application.ApplyResponse;
import ats.dto.application.ApplyUploadRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ApplicationService {

    ApplicationDetailResponse getDetail(Long applicationId, Principal principal);

    MoveApplicationStageResponse review(Long applicationId, ApplicationReviewRequest request, Principal principal);

    ApplyResponse applyUpload(Long jobId, ApplyUploadRequest request, MultipartFile file);
    MoveApplicationStageResponse moveStage(Long applicationId, MoveApplicationStageRequest request, Principal principal);

    InterviewScheduleResponse createInterview(Long applicationId, CreateInterviewRequest request, Principal principal);

    InterviewScheduleResponse updateInterview(Long applicationId, Long interviewId, CreateInterviewRequest request, Principal principal);
}
