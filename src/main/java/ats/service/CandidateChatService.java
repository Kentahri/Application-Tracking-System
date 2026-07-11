package ats.service;

import ats.dto.chat.CandidateChatRequest;
import ats.dto.chat.CandidateChatResponse;

import java.security.Principal;

public interface CandidateChatService {

    CandidateChatResponse chat(CandidateChatRequest request, Principal principal);
}
