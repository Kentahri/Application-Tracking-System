package ats.controller;

import ats.dto.cv.CvFileDownload;
import ats.service.RecruiterCvAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.security.Principal;

@RestController
@RequestMapping("/api/recruiter/cvs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recruiter CVs", description = "APIs for recruiters to access CV files of their job applications")
public class RecruiterCvController {

    private final RecruiterCvAccessService recruiterCvAccessService;

    @GetMapping("/{applicationId}/view")
    @Operation(
            summary = "View CV of an application",
            description = "Stream the CV file inline (PDF/images) or as attachment for the recruiter who owns the job"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CV file streamed"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied — not owner of the job"),
            @ApiResponse(responseCode = "404", description = "Application or CV not found")
    })
    public ResponseEntity<StreamingResponseBody> viewCv(
            @Parameter(description = "Application ID") @PathVariable Long applicationId,
            Principal principal) {
        return buildFileResponse(applicationId, principal, false);
    }

    @GetMapping("/{applicationId}/download")
    @Operation(
            summary = "Download CV of an application",
            description = "Stream the CV file as an attachment for the recruiter who owns the job"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CV file streamed as attachment"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Application or CV not found")
    })
    public ResponseEntity<StreamingResponseBody> downloadCv(
            @Parameter(description = "Application ID") @PathVariable Long applicationId,
            Principal principal) {
        return buildFileResponse(applicationId, principal, true);
    }

    private ResponseEntity<StreamingResponseBody> buildFileResponse(
            Long applicationId,
            Principal principal,
            boolean forceAttachment) {
        CvFileDownload file = recruiterCvAccessService.getApplicationCvFile(applicationId, principal);
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
