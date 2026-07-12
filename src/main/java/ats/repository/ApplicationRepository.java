package ats.repository;

import ats.entity.Application;
import ats.entity.Candidate;
import ats.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Application findByCandidateId_IdAndJobId_Id(Long candidateId, Long jobId);

    List<Application> findAllByCandidateId(Candidate candidate);

    long countByJobId_Id(Long jobId);

    @Query("""
            select a
            from Application a
            join fetch a.jobId j
            join fetch j.recruiterId
            join fetch a.pipelineStageId
            where a.id = :id
            """)
    Optional<Application> findByIdWithJobAndStage(@Param("id") Long id);

    @Query("""
            select a
            from Application a
            join fetch a.jobId j
            join fetch j.recruiterId
            join fetch a.candidateId
            join fetch a.cvId
            join fetch a.pipelineStageId
            where a.id = :id
            """)
    Optional<Application> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            select a
            from Application a
            join fetch a.candidateId
            join fetch a.cvId
            join fetch a.pipelineStageId
            where a.jobId.id = :jobId
            order by a.id desc
            """)
    List<Application> findByJobIdWithDetails(@Param("jobId") Long jobId);
}
