package ats.controller;

import ats.dto.chat.CandidateCvResponse;
import ats.dto.cv.CvFileDownload;
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
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
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

    @GetMapping("/{cvId}/download")
    @Operation(summary = "Download my CV", description = "Stream an owned CV from MinIO as an attachment")
    public ResponseEntity<StreamingResponseBody> downloadMyCv(
            @PathVariable Long cvId,
            Principal principal) {
        return buildFileResponse(cvId, principal, true);
    }

    @GetMapping("/{cvId}/view")
    @Operation(summary = "View my CV", description = "View an owned PDF/image inline; other types are downloaded")
    public ResponseEntity<StreamingResponseBody> viewMyCv(
            @PathVariable Long cvId,
            Principal principal) {
        return buildFileResponse(cvId, principal, false);
    }

    private ResponseEntity<StreamingResponseBody> buildFileResponse(
            Long cvId,
            Principal principal,
            boolean forceAttachment) {
        CvFileDownload file = candidateCvAccessService.getOwnedCvFile(cvId, principal);
        MediaType mediaType = parseMediaType(file.storedFile().contentType());
        boolean inlineSupported = MediaType.APPLICATION_PDF.equals(mediaType)
                || "image".equalsIgnoreCase(mediaType.getType());
        String dispositionType = forceAttachment || !inlineSupported ? "attachment" : "inline";

        ContentDisposition disposition = ContentDisposition.builder(dispositionType)
                .filename(file.fileName(), StandardCharsets.UTF_8)
                .build();

        StreamingResponseBody body = outputStream -> {
            try (file) {
                file.storedFile().inputStream().transferTo(outputStream);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(mediaType)
                .contentLength(file.storedFile().size())
                .body(body);
    }

    private MediaType parseMediaType(String contentType) {
            if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
