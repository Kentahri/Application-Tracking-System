package ats.controller;

import ats.dto.chat.CandidateChatRequest;
import ats.dto.chat.CandidateChatResponse;
import ats.service.CandidateChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/candidate/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Candidate Chat", description = "RAG chatbot APIs for candidates")
public class CandidateChatController {

    private final CandidateChatService candidateChatService;

    @PostMapping
    @Operation(summary = "Chat with candidate assistant", description = "Uses the authenticated candidate's own CV for job recommendation and CV improvement advice")
    public CandidateChatResponse chat(@Valid @RequestBody CandidateChatRequest request, Principal principal) {
        log.debug("REST request to candidate chat with cv id: {}, job id: {}", request.cvId(), request.jobId());
        return candidateChatService.chat(request, principal);
    }
}
