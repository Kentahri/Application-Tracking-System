package ats.service.impl;

import ats.dto.cv.CvFileDownload;
import ats.entity.Application;
import ats.entity.Cv;
import ats.entity.User;
import ats.exception.NotFoundException;
import ats.exception.UnauthorizedException;
import ats.repository.ApplicationRepository;
import ats.repository.UserRepository;
import ats.service.RecruiterCvAccessService;
import ats.storage.MinioStorage;
import ats.storage.StoredFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruiterCvAccessServiceImpl implements RecruiterCvAccessService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final MinioStorage minioStorage;

    @Override
    public CvFileDownload getApplicationCvFile(Long applicationId, Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new UnauthorizedException("Unauthorized");
        }

        Application application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> {
                    log.warn("Recruiter attempted to access CV for unavailable application id: {}", applicationId);
                    return new NotFoundException("Application not found");
                });

        validateRecruiterOwnsJob(principal, application);

        Cv cv = application.getCvId();
        if (cv == null || cv.getFilePath() == null || cv.getFilePath().isBlank()) {
            throw new NotFoundException("CV file not found");
        }

        StoredFile storedFile = minioStorage.getFile(cv.getFilePath());
        String fileName = extractFileName(cv.getFilePath(), cv.getId());

        return new CvFileDownload(storedFile, fileName);
    }

    private void validateRecruiterOwnsJob(Principal principal, Application application) {
        if (application.getJobId() == null || application.getJobId().getRecruiterId() == null) {
            throw new UnauthorizedException("Access denied");
        }

        String recruiterEmail = principal.getName();
        User recruiter = userRepository.findByEmail(recruiterEmail);
        if (recruiter == null) {
            log.warn("Authenticated recruiter not found with email: {}", recruiterEmail);
            throw new UnauthorizedException("Unauthorized");
        }

        Long recruiterIdFromJob = application.getJobId().getRecruiterId().getId();
        if (!recruiterIdFromJob.equals(recruiter.getId())) {
            log.warn("Recruiter {} attempted to access CV of application {} owned by recruiter {}",
                    recruiter.getId(), application.getId(), recruiterIdFromJob);
            throw new UnauthorizedException("Access denied");
        }
    }

    private String extractFileName(String objectKey, Long cvId) {
        int slash = objectKey.lastIndexOf('/');
        String fileName = slash >= 0 ? objectKey.substring(slash + 1) : objectKey;
        return fileName.isBlank() ? "cv-" + cvId : fileName;
    }
}
