package ats.repository;

import ats.entity.Application;
import ats.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Application findByCandidateId_IdAndJobId_Id(Long candidateId, Long jobId);

    List<Application> findAllByCandidateId(Candidate candidate);

    @Query(value = """
            select a from Application a
            join fetch a.jobId j join fetch a.pipelineStageId ps left join fetch a.cvId
            where a.candidateId.id = :candidateId
              and (:stage is null or lower(ps.stageName) = lower(:stage))
              and (:keyword is null or lower(j.title) like lower(concat('%', :keyword, '%')))
            """, countQuery = """
            select count(a) from Application a join a.jobId j join a.pipelineStageId ps
            where a.candidateId.id = :candidateId
              and (:stage is null or lower(ps.stageName) = lower(:stage))
              and (:keyword is null or lower(j.title) like lower(concat('%', :keyword, '%')))
            """)
    Page<Application> findCandidateHistory(@Param("candidateId") Long candidateId,
            @Param("stage") String stage, @Param("keyword") String keyword, Pageable pageable);

    @Query("""
            select a from Application a
            join fetch a.jobId join fetch a.pipelineStageId left join fetch a.cvId
            where a.id = :applicationId and a.candidateId.id = :candidateId
            """)
    Optional<Application> findCandidateApplicationDetail(@Param("applicationId") Long applicationId,
            @Param("candidateId") Long candidateId);

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
            order by coalesce(a.priority, 0) desc,
                     a.createdAt desc,
                     a.id desc
            """)
    List<Application> findByJobIdWithDetails(@Param("jobId") Long jobId);
}
