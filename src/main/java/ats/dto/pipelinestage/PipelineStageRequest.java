package ats.dto.pipelinestage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PipelineStageRequest {

    @Size(max = 100, message = "validation.pipelineStage.stageName.size")
    @NotBlank(message = "validation.pipelineStage.stageName.null")
    private String stageName;

    @NotNull(message = "validation.pipelineStage.stageOrder.null")
    @Positive(message = "validation.pipelineStage.stageOrder.positive")
    private Integer stageOrder;
}

