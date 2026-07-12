package ats.dto.application;

import ats.constant.InterviewResult;
import ats.constant.InterviewStatus;
import ats.constant.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApplicationDetailResponse {

    private Long applicationId;
    private Integer priority;
    private LocalDateTime appliedAt;
    private JobInfo job;
    private CandidateInfo candidate;
    private CvInfo cv;
    private StageInfo currentStage;
    private List<StageHistoryInfo> stageHistory;
    private InterviewInfo interview;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class JobInfo {
        private Long id;
        private String title;
        private String location;
        private BigDecimal salaryMin;
        private BigDecimal salaryMax;
        private JobStatus status;
    }

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
        private String fileType;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class StageInfo {
        private Long id;
        private String name;
        private Integer order;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class StageHistoryInfo {
        private Long id;
        private StageInfo fromStage;
        private StageInfo toStage;
        private String notes;
        private LocalDateTime movedAt;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class InterviewInfo {
        private Long id;
        private Long interviewerId;
        private String interviewerName;
        private LocalDateTime scheduledAt;
        private Integer durationMinutes;
        private String meetingLink;
        private InterviewStatus status;
        private InterviewResult result;
        private String feedback;
    }
}
