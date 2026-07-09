package ats.service.impl;

import ats.constant.UserStatus;
import ats.dto.chat.CandidateCvResponse;
import ats.entity.Candidate;
import ats.entity.Cv;
import ats.exception.NotFoundException;
import ats.exception.UnauthorizedException;
import ats.repository.CandidateRepository;
import ats.repository.CvRepository;
import ats.service.CandidateCvAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CandidateCvAccessServiceImpl implements CandidateCvAccessService {

    private final CandidateRepository candidateRepository;
    private final CvRepository cvRepository;

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
    public CandidateCvResponse getOwnedCvDetail(Long cvId, Principal principal) {
        return CandidateCvResponse.from(getOwnedCv(cvId, principal));
    }
}
