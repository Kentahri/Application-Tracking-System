package ats.dto.kanban;

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
public class KanbanApplicationResponse {

    private Long applicationId;
    private Integer priority;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private Long cvId;
    private String cvFilePath;
    private String cvFileType;
    private LocalDateTime appliedAt;
    private Long interviewId;
    private Long interviewerId;
    private String interviewerName;
    private LocalDateTime interviewScheduledAt;
    private InterviewStatus interviewStatus;
    private InterviewResult interviewResult;
}
