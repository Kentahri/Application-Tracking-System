package ats.service;

import ats.dto.chat.CandidateCvResponse;
import ats.entity.Candidate;
import ats.entity.Cv;

import java.security.Principal;

public interface CandidateCvAccessService {

    Candidate getCurrentCandidate(Principal principal);

    Cv getOwnedCv(Long cvId, Principal principal);

    CandidateCvResponse getOwnedCvDetail(Long cvId, Principal principal);
}
