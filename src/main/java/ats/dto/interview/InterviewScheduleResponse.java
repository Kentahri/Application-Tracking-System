package ats.dto.interview;

import ats.constant.InterviewResult;
import ats.constant.InterviewStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InterviewScheduleResponse {

    private Long id;
    private Long applicationId;
    private Long interviewerId;
    private String interviewerName;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private Long jobId;
    private String jobTitle;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String meetingLink;
    private InterviewStatus status;
    private InterviewResult result;
    private String feedback;
    private CandidateInfo candidate;
    private CvInfo cv;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class CandidateInfo {
        private Long id;
        private String name;
        private String email;
        private String phone;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class CvInfo {
        private Long id;
        private String filePath;
        private String fileName;
        private String fileType;
        private LocalDateTime parsedAt;
        private Boolean hasParsedText;
        private LocalDateTime createdAt;
    }
}
