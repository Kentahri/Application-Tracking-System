package ats.service.impl;

import ats.constant.UserStatus;
import ats.dto.chat.CandidateCvResponse;
import ats.dto.cv.CvFileDownload;
import ats.entity.Candidate;
import ats.entity.Cv;
import ats.exception.NotFoundException;
import ats.exception.UnauthorizedException;
import ats.repository.CandidateRepository;
import ats.repository.CvRepository;
import ats.service.CandidateCvAccessService;
import ats.storage.MinioStorage;
import ats.storage.StoredFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CandidateCvAccessServiceImpl implements CandidateCvAccessService {

    private final CandidateRepository candidateRepository;
    private final CvRepository cvRepository;
    private final MinioStorage minioStorage;

    @Override
    public Candidate getCurrentCandidate(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new UnauthorizedException("Unauthorized");
        }

        String email = principal.getName();
        Candidate candidate = candidateRepository.findByEmail(email);
        if (candidate == null) {
            log.warn("Authenticated candidate not found with email: {}", email);
            throw new UnauthorizedException("Unauthorized");
        }

        if (!UserStatus.ACTIVE.equals(candidate.getCandidateStatus())) {
            log.warn("Inactive candidate attempted CV access, candidate id: {}", candidate.getId());
            throw new UnauthorizedException("Unauthorized");
        }

        return candidate;
    }

    @Override
    public Cv getOwnedCv(Long cvId, Principal principal) {
        Candidate candidate = getCurrentCandidate(principal);
        return cvRepository.findByIdAndCandidateId_Id(cvId, candidate.getId())
                .orElseThrow(() -> {
                    log.warn("Candidate id: {} attempted to access unavailable CV id: {}", candidate.getId(), cvId);
                    return new NotFoundException("CV not found");
                });
    }

    @Override
    public List<CandidateCvResponse> getOwnedCvs(Principal principal) {
        Candidate candidate = getCurrentCandidate(principal);
        return cvRepository.findTop5ByCandidateId_IdOrderByCreatedAtDesc(candidate.getId())
                .stream()
                .map(CandidateCvResponse::from)
                .toList();
    }

    @Override
    public CandidateCvResponse getOwnedCvDetail(Long cvId, Principal principal) {
        return CandidateCvResponse.from(getOwnedCv(cvId, principal));
    }

    @Override
    public CvFileDownload getOwnedCvFile(Long cvId, Principal principal) {
        Cv cv = getOwnedCv(cvId, principal);
        if (cv.getFilePath() == null || cv.getFilePath().isBlank()) {
            throw new NotFoundException("CV file not found");
        }

        StoredFile storedFile = minioStorage.getFile(cv.getFilePath());
        return new CvFileDownload(storedFile, extractFileName(cv.getFilePath(), cvId));
    }

    private String extractFileName(String objectKey, Long cvId) {
        int slash = objectKey.lastIndexOf('/');
        String fileName = slash >= 0 ? objectKey.substring(slash + 1) : objectKey;
        return fileName.isBlank() ? "cv-" + cvId : fileName;
    }
}
