package ats.dto.chat;

import ats.entity.Cv;

import java.time.LocalDateTime;

public record CandidateCvResponse(
        Long id,
        String fileName,
        String fileType,
        LocalDateTime parsedAt,
        boolean hasParsedText
) {

    public static CandidateCvResponse from(Cv cv) {
        String parsedText = cv.getParsedText();
        return new CandidateCvResponse(
                cv.getId(),
                cv.getFileName(),
                cv.getFileType(),
                cv.getParsedAt(),
                parsedText != null && !parsedText.isBlank()
        );
    }
}
