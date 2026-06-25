package ats.service;

import ats.dto.pipelinestage.PipelineStageRequest;
import ats.dto.pipelinestage.PipelineStageResponse;
import ats.dto.pipelinestage.PipelineStageUpdateRequest;
import ats.http.PageResponse;
import ats.http.PagingRequest;

public interface PipelineStageService {

    PageResponse<PipelineStageResponse> getAllPipelineStages(PagingRequest pagingRequest);

    PipelineStageResponse getPipelineStageById(Long id);

    PipelineStageResponse create(PipelineStageRequest request);

    PipelineStageResponse update(Long id, PipelineStageUpdateRequest request);

    void delete(Long id);
}

