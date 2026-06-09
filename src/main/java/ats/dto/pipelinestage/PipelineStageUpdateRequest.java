package ats.dto.pipelinestage;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PipelineStageUpdateRequest {

    @Size(max = 100, message = "validation.pipelineStage.stageName.size")
    private String stageName;

    private Integer stageOrder;
}

