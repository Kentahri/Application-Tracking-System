package ats.service.impl;

import ats.constant.InterviewResult;
import ats.constant.InterviewStatus;
import ats.dto.interview.InterviewResultUpdateRequest;
import ats.dto.interview.InterviewScheduleResponse;
import ats.entity.Interview;
import ats.entity.User;
import ats.exception.BadRequestException;
import ats.exception.NotFoundException;
import ats.exception.UnauthorizedException;
import ats.helper.MessageHelper;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import ats.mapper.InterviewMapper;
import ats.repository.InterviewRepository;
import ats.repository.UserRepository;
import ats.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final InterviewMapper interviewMapper;

    @Override
    public PageResponse<InterviewScheduleResponse> getMySchedules(
            Principal principal,
            PagingRequest pagingRequest
    ) {
        User interviewer = getInterviewerFromPrincipal(principal);
        log.debug(
                "Getting interview schedules for interviewer id: {}, page: {}, size: {}",
                interviewer.getId(),
                pagingRequest.getPage(),
                pagingRequest.getSize()
        );

        Page<Interview> interviews = interviewRepository.findByInterviewerId_Id(
                interviewer.getId(),
                pagingRequest.toPageable(Sort.by(Sort.Direction.ASC, "scheduledAt"))
        );

        return PageResponse.of(interviews.map(interviewMapper::toScheduleResponse));
    }

    @Override
    @Transactional
    public InterviewScheduleResponse updateMyInterviewResult(
            Long interviewId,
            InterviewResultUpdateRequest request,
            Principal principal
    ) {
        User interviewer = getInterviewerFromPrincipal(principal);
        Interview interview = interviewRepository.findByIdAndInterviewerId_Id(
                        interviewId,
                        interviewer.getId()
                )
                .orElseThrow(() -> {
                    log.warn(
                            "Interview id: {} was not found for interviewer id: {}",
                            interviewId,
                            interviewer.getId()
                    );
                    return new NotFoundException(
                            MessageHelper.getMessage("error.interview.assigned.notFound", interviewId)
                    );
                });

        validateResultUpdate(interview, request);

        interview.setResult(request.getResult());
        if (request.getFeedback() != null) {
            interview.setFeedBack(normalizeFeedback(request.getFeedback()));
        }
        interview.setStatus(InterviewStatus.COMPLETED);
        interview.setUpdatedAt(LocalDateTime.now());

        Interview savedInterview = interviewRepository.save(interview);
        log.info(
                "Interviewer id: {} updated interview id: {} with result: {}",
                interviewer.getId(),
                interviewId,
                request.getResult()
        );
        return interviewMapper.toScheduleResponse(savedInterview);
    }

    private void validateResultUpdate(Interview interview, InterviewResultUpdateRequest request) {
        if (request.getResult() == InterviewResult.PENDING) {
            throw new BadRequestException(MessageHelper.getMessage("error.interview.result.pending"));
        }
        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new BadRequestException(MessageHelper.getMessage("error.interview.cancelled"));
        }
    }

    private String normalizeFeedback(String feedback) {
        if (feedback == null) {
            return null;
        }
        String normalizedFeedback = feedback.trim();
        return normalizedFeedback.isEmpty() ? null : normalizedFeedback;
    }

    private User getInterviewerFromPrincipal(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new UnauthorizedException(MessageHelper.getMessage("error.auth.unauthorized"));
        }

        String email = principal.getName();
        User interviewer = userRepository.findByEmail(email);
        if (interviewer == null) {
            log.warn("Authenticated interviewer not found with email: {}", email);
            throw new NotFoundException(MessageHelper.getMessage("error.user.email.notFound", email));
        }
        return interviewer;
    }
}
