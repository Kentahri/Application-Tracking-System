package ats.controller;

import ats.dto.pipelinestage.PipelineStageDeleteRequest;
import ats.dto.pipelinestage.PipelineStageRequest;
import ats.dto.pipelinestage.PipelineStageResponse;
import ats.dto.pipelinestage.PipelineStageUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Pipeline Stages", description = "APIs for admin pipeline stage management")
public class PipelineStageController {

    private final PipelineStageService pipelineStageService;

    @GetMapping
    @Operation(summary = "Get all pipeline stages", description = "Get all pipeline stages")
    @ApiResponse(responseCode = "200", description = "Pipeline stages retrieved successfully")
    public List<PipelineStageResponse> getAll() {
        log.debug("REST request to get all pipeline stages");
        return pipelineStageService.getAllPipelineStages();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pipeline stage by id", description = "Get pipeline stage detail by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pipeline stage retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Pipeline stage not found")
    })
    public PipelineStageResponse getById(@Parameter(description = "Pipeline stage id") @PathVariable Long id) {
        log.debug("REST request to get pipeline stage by id: {}", id);
        return pipelineStageService.getPipelineStageById(id);
    }

    @PostMapping
    @Operation(summary = "Create pipeline stage", description = "Create a new pipeline stage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pipeline stage created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public PipelineStageResponse create(@RequestBody PipelineStageRequest request) {
        log.debug("REST request to create pipeline stage: {}", request);
        return pipelineStageService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update pipeline stage", description = "Update an existing pipeline stage by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pipeline stage updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Pipeline stage not found")
    })
    public PipelineStageResponse update(@Parameter(description = "Pipeline stage id") @PathVariable Long id,
                                        @RequestBody PipelineStageUpdateRequest request) {
        log.debug("REST request to update pipeline stage id: {}", id);
        return pipelineStageService.update(id, request);
    }

    @DeleteMapping
    @Operation(summary = "Delete pipeline stage", description = "Soft delete a pipeline stage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pipeline stage deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Pipeline stage not found")
    })
    public void delete(@RequestBody PipelineStageDeleteRequest request) {
        log.debug("REST request to delete pipeline stage: {}", request.getId());
        pipelineStageService.delete(request);
    }
}

