package ats.controller;

import ats.dto.pipelinestage.PipelineStageDeleteRequest;
import ats.dto.pipelinestage.PipelineStageRequest;
import ats.dto.pipelinestage.PipelineStageResponse;
import ats.dto.pipelinestage.PipelineStageUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ats.service.PipelineStageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pipeline-stages")
@RequiredArgsConstructor
@Slf4j
public class PipelineStageController {

    private final PipelineStageService pipelineStageService;

    @GetMapping
    public List<PipelineStageResponse> getAll() {
        log.debug("REST request to get all pipeline stages");
        return pipelineStageService.getAllPipelineStages();
    }

    @GetMapping("/{id}")
    public PipelineStageResponse getById(@PathVariable Long id) {
        log.debug("REST request to get pipeline stage by id: {}", id);
        return pipelineStageService.getPipelineStageById(id);
    }

    @PostMapping
    public PipelineStageResponse create(@RequestBody PipelineStageRequest request) {
        log.debug("REST request to create pipeline stage: {}", request);
        return pipelineStageService.create(request);
    }

    @PutMapping("/{id}")
    public PipelineStageResponse update(@PathVariable Long id,
                                        @RequestBody PipelineStageUpdateRequest request) {
        log.debug("REST request to update pipeline stage id: {}", id);
        return pipelineStageService.update(id, request);
    }

    @DeleteMapping
    public void delete(@RequestBody PipelineStageDeleteRequest request) {
        log.debug("REST request to delete pipeline stage: {}", request.getId());
        pipelineStageService.delete(request);
    }
}

