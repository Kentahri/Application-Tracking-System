package ats.service.impl;

import ats.entity.Candidate;
import ats.entity.User;
import ats.repository.CandidateRepository;
import ats.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return new CustomUserDetails(user);
        }

        Candidate candidate = candidateRepository.findByEmail(email);
        if (candidate != null) {
            return new CandidateUserDetails(candidate);
        }

        throw new UsernameNotFoundException("Account not found with email: " + email);
    }
}
