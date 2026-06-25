package ats.service;

import ats.dto.application.MoveApplicationStageRequest;
import ats.dto.application.MoveApplicationStageResponse;

import java.security.Principal;

public interface ApplicationService {

    MoveApplicationStageResponse moveStage(Long applicationId, MoveApplicationStageRequest request, Principal principal);
}
