package ats.controller;

import ats.dto.pipelinestage.PipelineStageRequest;
import ats.dto.pipelinestage.PipelineStageResponse;
import ats.dto.pipelinestage.PipelineStageUpdateRequest;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/pipeline-stages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pipeline Stages", description = "APIs for admin pipeline stage management")
public class PipelineStageController {

    private final PipelineStageService pipelineStageService;

    @GetMapping
    @Operation(summary = "Get all pipeline stages", description = "Get paginated pipeline stages")
    @ApiResponse(responseCode = "200", description = "Pipeline stages retrieved successfully")
    public PageResponse<PipelineStageResponse> getAll(@Parameter(description = "Page index, starting from 0")
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @Parameter(description = "Number of records per page")
                                                      @RequestParam(defaultValue = "10") int size) {
        log.debug("REST request to get pipeline stages page: {}, size: {}", page, size);
        return pipelineStageService.getAllPipelineStages(new PagingRequest(page, size));
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
    public PipelineStageResponse create(@Valid @RequestBody PipelineStageRequest request) {
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
                                        @Valid @RequestBody PipelineStageUpdateRequest request) {
        log.debug("REST request to update pipeline stage id: {}", id);
        return pipelineStageService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete pipeline stage", description = "Soft delete a pipeline stage by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pipeline stage deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Pipeline stage not found")
    })
    public void delete(@Parameter(description = "Pipeline stage id") @PathVariable Long id) {
        log.debug("REST request to delete pipeline stage: {}", id);
        pipelineStageService.delete(id);
    }
}

