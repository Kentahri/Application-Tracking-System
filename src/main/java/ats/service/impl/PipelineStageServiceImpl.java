package ats.service.impl;

import ats.dto.pipelinestage.PipelineStageRequest;
import ats.dto.pipelinestage.PipelineStageResponse;
import ats.dto.pipelinestage.PipelineStageUpdateRequest;
import ats.entity.PipelineStage;
import ats.exception.BadRequestException;
import ats.exception.NotFoundException;
import ats.helper.MessageHelper;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import ats.mapper.PipelineStageMapper;
import ats.repository.PipelineStageRepository;
import ats.service.PipelineStageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PipelineStageServiceImpl implements PipelineStageService {

    private final PipelineStageRepository pipelineStageRepository;
    private final PipelineStageMapper pipelineStageMapper;

    private String message(String code, Object... args) {
        return MessageHelper.getMessage(code, args);
    }

    private PipelineStage getPipelineStageOrThrow(Long id) {
        return pipelineStageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Pipeline stage not found with id: {}", id);
                    return new NotFoundException(message("error.pipelineStage.notFound", id));
                });
    }

    @Override
    public PageResponse<PipelineStageResponse> getAllPipelineStages(PagingRequest pagingRequest) {
        log.debug("getting pipeline stages page: {}, size: {}", pagingRequest.getPage(), pagingRequest.getSize());

        Page<PipelineStage> pipelineStages = pipelineStageRepository.findAll(
                pagingRequest.toPageable(Sort.by(Sort.Direction.DESC, "id"))
        );
        Page<PipelineStageResponse> responses = pipelineStages.map(pipelineStageMapper::toDto);
        return PageResponse.of(responses);
    }

    @Override
    public PipelineStageResponse getPipelineStageById(Long id) {
        log.debug("getting pipeline stage by id: {}", id);

        PipelineStage pipelineStage = getPipelineStageOrThrow(id);
        return pipelineStageMapper.toDto(pipelineStage);
    }

    @Override
    @Transactional
    public PipelineStageResponse create(PipelineStageRequest request) {
        log.info("creating new pipeline stage with name: {}", request.getStageName());

        String stageName = request.getStageName().trim();

        if (pipelineStageRepository.existsByStageName(stageName)) {
            log.warn("Pipeline stage name already exists: {}", stageName);
            throw new BadRequestException(message("error.pipelineStage.name.exists"));
        }

        request.setStageName(stageName);

        PipelineStage pipelineStage = pipelineStageMapper.toEntity(request);
        PipelineStage saved = pipelineStageRepository.save(pipelineStage);

        log.info("created pipeline stage with id: {}", saved.getId());
        return pipelineStageMapper.toDto(saved);
    }

    @Override
    @Transactional
    public PipelineStageResponse update(Long id, PipelineStageUpdateRequest request) {
        log.info("updating pipeline stage with id: {}", id);

        PipelineStage pipelineStage = getPipelineStageOrThrow(id);

        if (request.getStageName() != null) {
            String stageName = request.getStageName().trim();
            if (stageName.isBlank()) {
                throw new BadRequestException(message("error.pipelineStage.name.blank"));
            }
            if (pipelineStageRepository.existsByStageNameAndIdNot(stageName, id)) {
                log.warn("Pipeline stage name already exists: {}", stageName);
                throw new BadRequestException(message("error.pipelineStage.name.exists"));
            }
            request.setStageName(stageName);
        }

        pipelineStageMapper.updateEntity(request, pipelineStage);

        log.info("updated pipeline stage id: {} with data: {}", id, request);
        return pipelineStageMapper.toDto(pipelineStage);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("deleting pipeline stage with id: {}", id);

        PipelineStage pipelineStage = getPipelineStageOrThrow(id);
        pipelineStageRepository.delete(pipelineStage);
        log.info("deleted pipeline stage with id: {}", id);
    }
}
