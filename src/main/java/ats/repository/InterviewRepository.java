package ats.repository;

import ats.entity.Interview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    Optional<Interview> findByApplicationId_Id(Long applicationId);

    @EntityGraph(attributePaths = {
            "interviewerId",
            "applicationId",
            "applicationId.candidateId",
            "applicationId.jobId"
    })
    Optional<Interview> findByIdAndInterviewerId_Id(Long id, Long interviewerId);

    @EntityGraph(attributePaths = {
            "interviewerId",
            "applicationId",
            "applicationId.candidateId",
            "applicationId.jobId"
    })
    Page<Interview> findByInterviewerId_Id(Long interviewerId, Pageable pageable);
}
