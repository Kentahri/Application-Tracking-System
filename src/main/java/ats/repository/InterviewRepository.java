package ats.repository;

import ats.entity.Interview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    Optional<Interview> findByApplicationId_Id(Long applicationId);

    @Query("""
            select i
            from Interview i
            join fetch i.interviewerId
            join fetch i.applicationId a
            join fetch a.candidateId
            join fetch a.jobId
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
