package ats.service;

import ats.dto.pipelinestage.PipelineStageDeleteRequest;
import ats.dto.pipelinestage.PipelineStageRequest;
import ats.dto.pipelinestage.PipelineStageResponse;
import ats.dto.pipelinestage.PipelineStageUpdateRequest;

import java.util.List;

public interface PipelineStageService {

    List<PipelineStageResponse> getAllPipelineStages();

    PipelineStageResponse getPipelineStageById(Long id);

    PipelineStageResponse create(PipelineStageRequest request);

    PipelineStageResponse update(Long id, PipelineStageUpdateRequest request);

    void delete(PipelineStageDeleteRequest request);
}

