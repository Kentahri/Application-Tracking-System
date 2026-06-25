package ats.dto.application;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MoveApplicationStageResponse {

    private Long applicationId;
    private Long fromStageId;
    private String fromStageName;
    private Long toStageId;
    private String toStageName;
    private LocalDateTime movedAt;
}
