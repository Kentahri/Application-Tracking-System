package ats.controller;

import ats.dto.auth.LoginResponse;
import ats.constant.PaymentStatus;
import ats.entity.Candidate;
import ats.entity.Payment;
import ats.repository.PaymentRepository;
import ats.service.impl.CandidateUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
public class CandidateAccountController {

    private final PaymentRepository paymentRepository;

    @Value("${payment.membership-duration-days:30}")
    private long membershipDurationDays;

    @GetMapping("/me")
    public LoginResponse.UserInfo getCurrentCandidate(Authentication authentication) {
        CandidateUserDetails principal = (CandidateUserDetails) authentication.getPrincipal();
        Candidate candidate = principal.getCandidate();
        Payment latestPayment = paymentRepository
                .findTopByCandidateId_IdAndStatusAndIsDeletedFalseOrderByPaidAtDesc(
                        candidate.getId(), PaymentStatus.SUCCESS)
                .orElse(null);

        return new LoginResponse.UserInfo(
                candidate.getId(),
                candidate.getEmail(),
                candidate.getName(),
                ats.constant.UserRole.CANDIDATE,
                candidate.getNumberOfQueryQuota(),
                candidate.getUpgradePackageId() != null
                        ? candidate.getUpgradePackageId().getPackageName()
                        : null,
                candidate.getUpgradePackageId() != null
                        ? candidate.getUpgradePackageId().getPriority()
                        : null,
                latestPayment != null && latestPayment.getPaidAt() != null
                        ? latestPayment.getPaidAt().plusDays(membershipDurationDays)
                        : null
        );
    }
}
