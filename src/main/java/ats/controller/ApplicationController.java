package ats.controller;

import ats.dto.application.ApplicationDetailResponse;
import ats.dto.application.ApplicationReviewRequest;
import ats.dto.application.MoveApplicationStageRequest;
import ats.dto.application.MoveApplicationStageResponse;
import ats.dto.interview.CreateInterviewRequest;
import ats.dto.interview.InterviewScheduleResponse;
import ats.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get application detail", description = "Get application detail with candidate, CV, stage history, and interview")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application detail retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated or access denied"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ApplicationDetailResponse getDetail(
            @Parameter(description = "Application id") @PathVariable Long applicationId,
            Principal principal) {
        log.debug("REST request to get application detail id: {}", applicationId);
        return applicationService.getDetail(applicationId, principal);
    }

    @PostMapping("/{applicationId}/review")
    @Operation(summary = "Review application", description = "Review an application in Applied stage and move it to Interview or Rejected")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application reviewed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid stage, decision, or closed job"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated or access denied"),
            @ApiResponse(responseCode = "404", description = "Application or pipeline stage not found")
    })
    public MoveApplicationStageResponse review(
            @Parameter(description = "Application id") @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationReviewRequest request,
            Principal principal) {
        log.debug("REST request to review application id: {} with decision: {}", applicationId, request.getDecision());
        return applicationService.review(applicationId, request, principal);
    }

    @PostMapping("/{applicationId}/interviews")
    @Operation(summary = "Create interview", description = "Create an interview schedule for an application in Interview stage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Interview created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid stage, interviewer, request, or closed job"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated or access denied"),
            @ApiResponse(responseCode = "404", description = "Application or interviewer not found")
    })
    public InterviewScheduleResponse createInterview(
            @Parameter(description = "Application id") @PathVariable Long applicationId,
            @Valid @RequestBody CreateInterviewRequest request,
            Principal principal) {
        log.debug(
                "REST request to create interview for application id: {} with interviewer id: {}",
                applicationId,
                request.getInterviewerId()
        );
        return applicationService.createInterview(applicationId, request, principal);
    }

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
