package ats.dto.pipelinestage;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PipelineStageDeleteRequest {

    @NotNull(message = "validation.pipelineStage.id.null")
    private Long id;
}

