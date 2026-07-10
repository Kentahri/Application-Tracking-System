package ats.service.impl;

import ats.constant.UserStatus;
import ats.entity.Candidate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CandidateUserDetails implements UserDetails {

    private final Candidate candidate;

    public CandidateUserDetails(Candidate candidate) {
        this.candidate = candidate;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CANDIDATE"));
    }

    @Override
    public String getPassword() {
        return candidate.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return candidate.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return UserStatus.ACTIVE.equals(candidate.getCandidateStatus());
    }
}
