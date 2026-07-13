package ats.dto.candidateapplication;

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

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CandidateApplicationResponse {
    private Long applicationId;
    private LocalDateTime appliedAt;
    private LocalDateTime lastUpdatedAt;
    private JobInfo job;
    private StageInfo currentStage;
    private CvInfo cv;
    private boolean hasInterview;
    private List<StageHistoryInfo> stageHistory;
    private InterviewInfo interview;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class JobInfo { private Long id; private String title; private String location; private BigDecimal salaryMin; private BigDecimal salaryMax; private JobStatus status; }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class StageInfo { private Long id; private String name; private Integer order; }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CvInfo { private Long id; private String fileName; private String fileType; }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class StageHistoryInfo { private Long id; private StageInfo fromStage; private StageInfo toStage; private String notes; private LocalDateTime movedAt; }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class InterviewInfo { private Long id; private LocalDateTime scheduledAt; private Integer durationMinutes; private String meetingLink; private InterviewStatus status; private InterviewResult result; private String feedback; }
}
