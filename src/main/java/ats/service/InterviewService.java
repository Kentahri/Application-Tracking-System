package ats.service;

import ats.dto.interview.InterviewResultUpdateRequest;
import ats.dto.interview.InterviewScheduleResponse;
import ats.http.PageResponse;
import ats.http.PagingRequest;

import java.security.Principal;

public interface InterviewService {

    PageResponse<InterviewScheduleResponse> getMySchedules(
            Principal principal,
            PagingRequest pagingRequest
    );

    InterviewScheduleResponse updateMyInterviewResult(
            Long interviewId,
            InterviewResultUpdateRequest request,
            Principal principal
    );
}
