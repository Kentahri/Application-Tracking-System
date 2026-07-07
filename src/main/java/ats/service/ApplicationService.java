package ats.service;

import ats.dto.application.MoveApplicationStageRequest;
import ats.dto.application.MoveApplicationStageResponse;

import java.security.Principal;
import ats.dto.application.ApplyResponse;
import ats.dto.application.ApplyUploadRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ApplicationService {

    ApplyResponse applyUpload(Long jobId, ApplyUploadRequest request, MultipartFile file);
    MoveApplicationStageResponse moveStage(Long applicationId, MoveApplicationStageRequest request, Principal principal);
}
