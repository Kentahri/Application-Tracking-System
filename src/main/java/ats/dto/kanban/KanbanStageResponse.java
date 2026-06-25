package ats.dto.kanban;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class KanbanStageResponse {

    private Long stageId;
    private String stageName;
    private Integer stageOrder;
    private Long totalApplications;
    private List<KanbanApplicationResponse> applications;
}
