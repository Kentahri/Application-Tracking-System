package ats.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ats.dto.pipelinestage.PipelineStageDeleteRequest;
import ats.dto.pipelinestage.PipelineStageRequest;
import ats.dto.pipelinestage.PipelineStageResponse;
import ats.dto.pipelinestage.PipelineStageUpdateRequest;
import ats.entity.PinelineStage;
import ats.mapper.PipelineStageMapper;
import ats.repository.PipelineStageRepository;
import ats.service.PipelineStageService;

import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PipelineStageServiceImpl implements PipelineStageService {

    private final PipelineStageRepository pipelineStageRepository;
    private final PipelineStageMapper pipelineStageMapper;

    private PinelineStage getPipelineStageOrThrow(Long id) {
        return pipelineStageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Pipeline stage not found with id: {}", id);
                    return new RuntimeException("Không tìm thấy giai đoạn với id: " + id);
                });
    }

    @Override
    public List<PipelineStageResponse> getAllPipelineStages() {
        log.debug("getting all pipeline stages");
        List<PinelineStage> pipelineStages = pipelineStageRepository.findAll();
        List<PipelineStageResponse> responses = pipelineStageMapper.toDto(pipelineStages);
        return responses;
    }

    @Override
    public PipelineStageResponse getPipelineStageById(Long id) {
        log.debug("getting pipeline stage by id: {}", id);

        PinelineStage pipelineStage = getPipelineStageOrThrow(id);
        PipelineStageResponse response = pipelineStageMapper.toDto(pipelineStage);
        return response;
    }

    @Override
    @Transactional
    public PipelineStageResponse create(PipelineStageRequest request) {
        log.info("creating new pipeline stage with name: {}", request.getStageName());

        if(pipelineStageRepository.existsByStageName(request.getStageName())) {
            log.warn("Pipeline stage name already exists: {}", request.getStageName());
            throw new RuntimeException("Tên giai đoạn đã tồn tại");
        }

        PinelineStage pipelineStage = pipelineStageMapper.toEntity(request);
        PinelineStage saved = pipelineStageRepository.save(pipelineStage);

        log.info("created pipeline stage with id: {}", saved.getId());
        return pipelineStageMapper.toDto(saved);
    }

    @Override
    @Transactional
    public PipelineStageResponse update(Long id, PipelineStageUpdateRequest request) {
        log.info("updating pipeline stage with id: {}", id);

        PinelineStage pipelineStage = getPipelineStageOrThrow(id);

        pipelineStageMapper.updateEntity(request, pipelineStage);

        log.info("updated pipeline stage id: {} with data: {}", id, request);
        return pipelineStageMapper.toDto(pipelineStage);
    }

    @Override
    @Transactional
    public void delete(PipelineStageDeleteRequest request) {
        log.info("deleting pipeline stage with id: {}", request.getId());

        PinelineStage pipelineStage = getPipelineStageOrThrow(request.getId());
        pipelineStageRepository.delete(pipelineStage);
        log.info("deleted pipeline stage with id: {}", request.getId());
    }
}

