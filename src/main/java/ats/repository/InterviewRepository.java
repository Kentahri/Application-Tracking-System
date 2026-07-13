package ats.repository;

import ats.entity.Interview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    Optional<Interview> findByApplicationId_Id(Long applicationId);

    boolean existsByApplicationId_Id(Long applicationId);

    @Query("""
            select i
            from Interview i
            join fetch i.interviewerId
            where i.applicationId.id = :applicationId
            """)
    Optional<Interview> findByApplicationIdWithInterviewer(@Param("applicationId") Long applicationId);

    @Query("""
            select i
            from Interview i
            join fetch i.interviewerId
            join fetch i.applicationId a
            join fetch a.candidateId
            join fetch a.jobId
            where i.id = :interviewId
              and a.id = :applicationId
            """)
    Optional<Interview> findByIdAndApplicationIdWithDetails(
            @Param("interviewId") Long interviewId,
            @Param("applicationId") Long applicationId
    );

    @Query("""
            select i
            from Interview i
            join fetch i.interviewerId
            join fetch i.applicationId a
            where a.jobId.id = :jobId
            """)
    List<Interview> findByJobIdWithInterviewer(@Param("jobId") Long jobId);

    @Query("""
            select i
            from Interview i
            join fetch i.interviewerId
            join fetch i.applicationId a
            join fetch a.candidateId
            join fetch a.jobId
            left join fetch a.cvId
            where i.id = :id
              and i.interviewerId.id = :interviewerId
            """)
    Optional<Interview> findAssignedInterviewById(
            @Param("id") Long id,
            @Param("interviewerId") Long interviewerId
    );

    @Query(
            value = """
                    select i
                    from Interview i
                    join fetch i.interviewerId
                    join fetch i.applicationId a
                    join fetch a.candidateId
                    join fetch a.jobId
                    left join fetch a.cvId
                    where i.interviewerId.id = :interviewerId
                    """,
            countQuery = """
                    select count(i)
                    from Interview i
                    where i.interviewerId.id = :interviewerId
                    """
    )
    Page<Interview> findAssignedInterviews(
            @Param("interviewerId") Long interviewerId,
            Pageable pageable
    );
}
