package ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ats.entity.Payment;
import ats.constant.PaymentStatus;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionIdAndIsDeletedFalse(String transactionId);

    Optional<Payment> findByTransactionIdAndCandidateId_IdAndIsDeletedFalse(
            String transactionId,
            Long candidateId);

    Optional<Payment> findByOrderIdAndIsDeletedFalse(String orderId);

    Optional<Payment> findTopByCandidateId_IdAndStatusAndIsDeletedFalseOrderByPaidAtDesc(
            Long candidateId,
            PaymentStatus status);
}
