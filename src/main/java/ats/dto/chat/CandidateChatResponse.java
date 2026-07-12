package ats.dto.chat;

import java.util.List;

public record CandidateChatResponse(
        String answer,
        List<JobSuggestionResponse> jobs,
        Integer numberOfQueryQuota
) {
}
