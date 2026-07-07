package ats.controller;

import ats.dto.application.ApplyResponse;
import ats.dto.application.ApplyUploadRequest;
import ats.dto.job.JobResponse;
import ats.http.ApiResponse;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import ats.http.ResponseBuilder;
import ats.service.ApplicationService;
import ats.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public Jobs", description = "Public APIs for job seekers")
public class PublicJobController {

    private final ApplicationService applicationService;
    private final JobService jobService;

    @PostMapping(value = "/{jobId}/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Apply for a job by uploading CV file",
            description = "Public endpoint. The candidate submits a multipart request with a JSON part 'data' "
                    + "(fullName, email, phone, message) and a file part 'cvFile'. "
                    + "The server stores the file in MinIO; only filePath is kept in DB."
    )
    public ResponseEntity<ApiResponse<ApplyResponse>> apply(
            @PathVariable Long jobId,
            @Valid @RequestPart("data") ApplyUploadRequest request,
            @RequestPart("cvFile") MultipartFile cvFile) {
        return ResponseBuilder.ok(applicationService.applyUpload(jobId, request, cvFile));
    }

    @GetMapping("/getAll")
    @Operation(summary = "Get all posted jobs", description = "Get paginated job postings that are currently open for applications (status = PUBLISHED)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Posted jobs retrieved successfully")
    public PageResponse<JobResponse> getAll(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        log.debug("REST request to get posted jobs page: {}, size: {}", page, size);
        return jobService.getAllPostedJobs(new PagingRequest(page, size));
    }
}
