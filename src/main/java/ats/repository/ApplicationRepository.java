package ats.repository;

import ats.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Application findByCandidateId_IdAndJobId_Id(Long candidateId, Long jobId);
}
