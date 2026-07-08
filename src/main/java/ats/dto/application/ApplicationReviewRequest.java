package ats.dto.application;

import ats.constant.ApplicationReviewDecision;
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
public class ApplicationReviewRequest {

    @NotNull(message = "{validation.application.reviewDecision.null}")
    private ApplicationReviewDecision decision;

    @Size(max = 2000, message = "{validation.application.notes.size}")
    private String notes;
}
