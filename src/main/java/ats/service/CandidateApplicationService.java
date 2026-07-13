package ats.service;
import ats.dto.candidateapplication.CandidateApplicationResponse;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import java.security.Principal;
public interface CandidateApplicationService {
    PageResponse<CandidateApplicationResponse> getMyApplications(Principal principal, PagingRequest pagingRequest, String stage, String keyword);
    CandidateApplicationResponse getMyApplicationDetail(Long applicationId, Principal principal);
}
