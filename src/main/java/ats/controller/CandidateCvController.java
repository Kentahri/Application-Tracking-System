package ats.controller;

import ats.dto.chat.CandidateCvResponse;
import ats.service.CandidateCvAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/candidate/cvs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Candidate CVs", description = "APIs for candidates to access their own CVs")
public class CandidateCvController {

    private final CandidateCvAccessService candidateCvAccessService;

    @GetMapping
    @Operation(
            summary = "Get my CVs",
            description = "Get up to 5 most recent CVs uploaded by the authenticated candidate"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CVs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public List<CandidateCvResponse> getMyCvs(Principal principal) {
        log.debug("REST request to get CVs for authenticated candidate");
        return candidateCvAccessService.getOwnedCvs(principal);
    }

    @GetMapping("/{cvId}")
    @Operation(
            summary = "Get my CV by id",
            description = "Get CV detail by id for the authenticated candidate"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CV retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "CV not found")
    })
    public CandidateCvResponse getMyCvById(
            @Parameter(description = "CV id") @PathVariable Long cvId,
            Principal principal) {
        log.debug("REST request to get CV id: {} for authenticated candidate", cvId);
        return candidateCvAccessService.getOwnedCvDetail(cvId, principal);
    }
}
