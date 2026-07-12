package ats.service;

import ats.dto.candidate.CandidateRequest;
import ats.dto.candidate.CandidateResponse;

public interface CandidateService {

    CandidateResponse create(CandidateRequest request);
}
