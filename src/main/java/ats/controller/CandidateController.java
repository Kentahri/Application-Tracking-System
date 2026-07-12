package ats.controller;

import ats.dto.candidate.CandidateRequest;
import ats.dto.candidate.CandidateResponse;
import ats.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Candidates", description = "APIs for candidate accounts")
public class CandidateController {

    private final CandidateService candidateService;

    @PostMapping
    @Operation(summary = "Create candidate account", description = "Create a new candidate account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Candidate created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public CandidateResponse create(@Valid @RequestBody CandidateRequest request) {
        log.debug("REST request to create candidate with email: {}", request.getEmail());
        return candidateService.create(request);
    }
}
