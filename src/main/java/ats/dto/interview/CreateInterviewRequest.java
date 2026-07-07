package ats.dto.interview;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateInterviewRequest {

    @NotNull(message = "{validation.interview.interviewerId.null}")
    private Long interviewerId;

    @NotNull(message = "{validation.interview.scheduledAt.null}")
    private LocalDateTime scheduledAt;

    @NotNull(message = "{validation.interview.durationMinutes.null}")
    @Positive(message = "{validation.interview.durationMinutes.positive}")
    private Integer durationMinutes;

    @Size(max = 1000, message = "{validation.interview.meetingLink.size}")
    private String meetingLink;

    @Size(max = 2000, message = "{validation.interview.feedback.size}")
    private String note;
}
