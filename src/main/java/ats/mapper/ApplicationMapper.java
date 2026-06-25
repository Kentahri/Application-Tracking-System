package ats.mapper;

import ats.dto.application.MoveApplicationStageResponse;
import ats.entity.Application;
import ats.entity.PipelineStage;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    default MoveApplicationStageResponse toMoveStageResponse(Application application,
                                                             PipelineStage fromStage,
                                                             PipelineStage toStage,
                                                             LocalDateTime movedAt) {
        return new MoveApplicationStageResponse(
                application.getId(),
                fromStage.getId(),
                fromStage.getStageName(),
                toStage.getId(),
                toStage.getStageName(),
                movedAt
        );
    }
}
