package ats.repository;

import ats.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Candidate findByEmail(String email);

    @Modifying
    @Query("""
            update Candidate candidate
            set candidate.numberOfQueryQuota = candidate.numberOfQueryQuota - 1,
                candidate.updatedAt = :updatedAt
            where candidate.id = :candidateId
              and candidate.numberOfQueryQuota > 0
            """)
    int consumeQueryQuota(@Param("candidateId") Long candidateId, @Param("updatedAt") LocalDateTime updatedAt);
}
