package ats.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ats.dto.pipelinestage.PipelineStageRequest;
import ats.dto.pipelinestage.PipelineStageResponse;
import ats.dto.pipelinestage.PipelineStageUpdateRequest;
import ats.entity.PinelineStage;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PipelineStageMapper {

    PipelineStageResponse toDto(PinelineStage pipelineStage);

    List<PipelineStageResponse> toDto(List<PinelineStage> pipelineStages);

    PinelineStage toEntity(PipelineStageRequest pipelineStageRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(PipelineStageUpdateRequest pipelineStageUpdateRequest, @MappingTarget PinelineStage pipelineStage);
}

