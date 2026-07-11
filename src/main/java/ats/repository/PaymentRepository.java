package ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ats.entity.Payment;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionIdAndIsDeletedFalse(String transactionId);

    Optional<Payment> findByOrderIdAndIsDeletedFalse(String orderId);
}
