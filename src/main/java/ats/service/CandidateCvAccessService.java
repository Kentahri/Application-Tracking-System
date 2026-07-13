package ats.service;

import ats.dto.chat.CandidateCvResponse;
import ats.dto.cv.CvFileDownload;
import ats.entity.Candidate;
import ats.entity.Cv;

import java.security.Principal;
import java.util.List;

public interface CandidateCvAccessService {

    Candidate getCurrentCandidate(Principal principal);

    Cv getOwnedCv(Long cvId, Principal principal);

    List<CandidateCvResponse> getOwnedCvs(Principal principal);

    CandidateCvResponse getOwnedCvDetail(Long cvId, Principal principal);

    CvFileDownload getOwnedCvFile(Long cvId, Principal principal);
}
