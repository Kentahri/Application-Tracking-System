package ats.dto.chat;

import jakarta.validation.constraints.NotBlank;

public record CandidateChatRequest(
        @NotBlank(message = "Message is required")
        String message,

        Long cvId,

        Long jobId
) {
}
