package ats.mapper;

import ats.entity.PipelineStage;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ats.dto.pipelinestage.PipelineStageRequest;
import ats.dto.pipelinestage.PipelineStageResponse;
import ats.dto.pipelinestage.PipelineStageUpdateRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PipelineStageMapper {

    PipelineStageResponse toDto(PipelineStage pipelineStage);

    List<PipelineStageResponse> toDto(List<PipelineStage> pipelineStages);

    PipelineStage toEntity(PipelineStageRequest pipelineStageRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(PipelineStageUpdateRequest pipelineStageUpdateRequest, @MappingTarget PipelineStage pipelineStage);
}

