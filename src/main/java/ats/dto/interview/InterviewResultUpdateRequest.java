package ats.dto.interview;

import ats.constant.InterviewResult;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InterviewResultUpdateRequest {

    @NotNull(message = "{validation.interview.result.null}")
    private InterviewResult result;

    @Size(max = 2000, message = "{validation.interview.feedback.size}")
    private String feedback;
}
