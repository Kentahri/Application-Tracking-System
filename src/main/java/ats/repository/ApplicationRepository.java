package ats.repository;

import ats.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Application findByCandidateId_IdAndJobId_Id(Long candidateId, Long jobId);

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
