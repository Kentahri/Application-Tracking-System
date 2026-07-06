package ats.controller;

import ats.dto.interview.InterviewResultUpdateRequest;
import ats.dto.interview.InterviewScheduleResponse;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import ats.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/interviewer/schedules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Interviewer Schedules", description = "APIs for interviewers to view assigned schedules")
public class InterviewScheduleController {

    private final InterviewService interviewService;

    @GetMapping
    @Operation(
            summary = "Get my interview schedules",
            description = "Get schedules assigned to the authenticated interviewer"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Interview schedules retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public PageResponse<InterviewScheduleResponse> getMySchedules(
            @Parameter(description = "Page number, starting from 1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of records per page")
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    ) {
        log.debug("REST request to get interviewer's schedules, page: {}, size: {}", page, size);
        return interviewService.getMySchedules(principal, new PagingRequest(page, size));
    }

    @PostMapping("/{interviewId}/result")
    @Operation(
            summary = "Update my interview result",
            description = "Update result and feedback for an interview assigned to the authenticated interviewer"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Interview result updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid result or cancelled interview"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Assigned interview not found")
    })
    public InterviewScheduleResponse updateResult(
            @Parameter(description = "Interview id") @PathVariable Long interviewId,
            @Valid @RequestBody InterviewResultUpdateRequest request,
            Principal principal
    ) {
        log.debug(
                "REST request to update result for interview id: {} with result: {}",
                interviewId,
                request.getResult()
        );
        return interviewService.updateMyInterviewResult(interviewId, request, principal);
    }
}
