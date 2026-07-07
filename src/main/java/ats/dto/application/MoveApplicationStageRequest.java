package ats.dto.application;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MoveApplicationStageRequest {

    @NotNull(message = "{validation.application.toStageId.null}")
    private Long toStageId;
}
