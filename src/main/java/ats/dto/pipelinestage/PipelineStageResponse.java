package ats.dto.pipelinestage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PipelineStageResponse {
    private Long id;
    private String stageName;
    private Integer stageOrder;
}

