package ats.service.impl;

import ats.constant.UserStatus;
import ats.dto.candidate.CandidateRequest;
import ats.dto.candidate.CandidateResponse;
import ats.entity.Candidate;
import ats.exception.BadRequestException;
import ats.helper.MessageHelper;
import ats.mapper.CandidateMapper;
import ats.repository.CandidateRepository;
import ats.repository.UserRepository;
import ats.service.CandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final CandidateMapper candidateMapper;
    private final PasswordEncoder passwordEncoder;

    private String message(String code, Object... args) {
        return MessageHelper.getMessage(code, args);
    }

    @Override
    @Transactional
    public CandidateResponse create(CandidateRequest request) {
        if (candidateRepository.existsByEmail(request.getEmail()) || userRepository.existsByEmail(request.getEmail())) {
            log.warn("Candidate email already exists: {}", request.getEmail());
            throw new BadRequestException(message("error.candidate.email.exists"));
        }

        Candidate candidate = candidateMapper.toEntity(request);
        candidate.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        candidate.setCandidateStatus(UserStatus.ACTIVE);

        Candidate saved = candidateRepository.save(candidate);
        log.info("created candidate with id: {}", saved.getId());
        return candidateMapper.toDto(saved);
    }
}
