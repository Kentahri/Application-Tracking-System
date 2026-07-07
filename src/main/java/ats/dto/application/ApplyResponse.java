package ats.dto.application;

public record ApplyResponse(
        Long applicationId,
        Long cvId,
        String fileName,
        String storedKey,
        String contentType
) {}
