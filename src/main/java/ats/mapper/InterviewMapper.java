package ats.mapper;

import ats.dto.interview.InterviewScheduleResponse;
import ats.entity.Application;
import ats.entity.Candidate;
import ats.entity.Cv;
import ats.entity.Interview;
import ats.entity.Job;
import ats.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InterviewMapper {

    default InterviewScheduleResponse toScheduleResponse(Interview interview) {
        if (interview == null) {
            return null;
        }

        Application application = interview.getApplicationId();
        User interviewer = interview.getInterviewerId();
        Candidate candidate = application != null ? application.getCandidateId() : null;
        Cv cv = application != null ? application.getCvId() : null;
        Job job = application != null ? application.getJobId() : null;

        InterviewScheduleResponse response = new InterviewScheduleResponse();
        response.setId(interview.getId());
        response.setApplicationId(application != null ? application.getId() : null);
        response.setInterviewerId(interviewer != null ? interviewer.getId() : null);
        response.setInterviewerName(interviewer != null ? interviewer.getName() : null);
        response.setCandidateId(candidate != null ? candidate.getId() : null);
        response.setCandidateName(candidate != null ? candidate.getName() : null);
        response.setCandidateEmail(candidate != null ? candidate.getEmail() : null);
        response.setCandidatePhone(candidate != null ? candidate.getPhone() : null);
        response.setJobId(job != null ? job.getId() : null);
        response.setJobTitle(job != null ? job.getTitle() : null);
        response.setScheduledAt(interview.getScheduledAt());
        response.setDurationMinutes(interview.getDurationMinutes());
        response.setMeetingLink(interview.getMeetingLink());
        response.setStatus(interview.getStatus());
        response.setResult(interview.getResult());
        response.setFeedback(interview.getFeedBack());
        response.setCandidate(toCandidateInfo(candidate));
        response.setCv(toCvInfo(cv));
        return response;
    }

    default InterviewScheduleResponse.CandidateInfo toCandidateInfo(Candidate candidate) {
        if (candidate == null) {
            return null;
        }
        return new InterviewScheduleResponse.CandidateInfo(
                candidate.getId(),
                candidate.getName(),
                candidate.getEmail(),
                candidate.getPhone()
        );
    }

    default InterviewScheduleResponse.CvInfo toCvInfo(Cv cv) {
        if (cv == null) {
            return null;
        }
        String parsedText = cv.getParsedText();
        return new InterviewScheduleResponse.CvInfo(
                cv.getId(),
                cv.getFilePath(),
                cv.getFileName(),
                cv.getFileType(),
                cv.getParsedAt(),
                parsedText != null && !parsedText.isBlank(),
                cv.getCreatedAt()
        );
    }
}
