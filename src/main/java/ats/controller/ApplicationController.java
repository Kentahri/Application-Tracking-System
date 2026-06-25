package ats.controller;

import ats.dto.application.MoveApplicationStageRequest;
import ats.dto.application.MoveApplicationStageResponse;
import ats.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/recruiter/applications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recruiter Applications", description = "APIs for recruiters to manage applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PatchMapping("/{applicationId}/stage")
    @Operation(summary = "Move application stage", description = "Move an application to another pipeline stage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application stage moved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or job is closed"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated or access denied"),
            @ApiResponse(responseCode = "404", description = "Application or pipeline stage not found")
    })
    public MoveApplicationStageResponse moveStage(
            @Parameter(description = "Application id") @PathVariable Long applicationId,
            @Valid @RequestBody MoveApplicationStageRequest request,
            Principal principal) {
        log.debug("REST request to move application id: {} to stage id: {}", applicationId, request.getToStageId());
        return applicationService.moveStage(applicationId, request, principal);
    }
}
