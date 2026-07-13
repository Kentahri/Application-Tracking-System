package ats.controller;
import ats.dto.candidateapplication.CandidateApplicationResponse;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import ats.service.CandidateApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController @RequestMapping("/api/candidate/applications") @RequiredArgsConstructor
@Tag(name = "Candidate Applications", description = "Candidate application history APIs")
public class CandidateApplicationController {
    private final CandidateApplicationService candidateApplicationService;
    @GetMapping @Operation(summary = "Get my application history")
    public PageResponse<CandidateApplicationResponse> getMyApplications(PagingRequest pagingRequest,
            @RequestParam(required = false) String stage, @RequestParam(required = false) String keyword, Principal principal) {
        return candidateApplicationService.getMyApplications(principal, pagingRequest, stage, keyword);
    }
    @GetMapping("/{applicationId}") @Operation(summary = "Get my application detail")
    public CandidateApplicationResponse getMyApplicationDetail(@PathVariable Long applicationId, Principal principal) {
        return candidateApplicationService.getMyApplicationDetail(applicationId, principal);
    }
}
