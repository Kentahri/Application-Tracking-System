package ats.service;

import ats.dto.cv.CvFileDownload;

import java.security.Principal;

public interface RecruiterCvAccessService {

    CvFileDownload getApplicationCvFile(Long applicationId, Principal principal);
}
