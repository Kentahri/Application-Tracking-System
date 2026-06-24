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
public class KanbanBoardResponse {

    private Long jobId;
    private String jobTitle;
    private Long totalApplications;
    private List<KanbanStageResponse> stages;
}
