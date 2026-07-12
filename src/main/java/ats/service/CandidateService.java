package ats.service;

import ats.dto.candidate.CandidateChangePasswordRequest;
import ats.dto.candidate.CandidateRequest;
import ats.dto.candidate.CandidateResponse;

import java.security.Principal;

public interface CandidateService {

    CandidateResponse create(CandidateRequest request);

    CandidateResponse changePassword(Long id, CandidateChangePasswordRequest request, Principal principal);
}
