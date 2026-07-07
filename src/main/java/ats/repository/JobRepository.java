package ats.repository;

import ats.constant.JobStatus;
import ats.repository.projection.JobWithApplicationCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import ats.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface JobRepository extends JpaRepository<Job, Long> {

    boolean existsByTitle(String title);

    boolean existsByTitleAndIdNot(String title, Long id);

    Job findByTitle(String title);

    Page<Job> findByRecruiterId_Id(Long recruiterId, Pageable pageable);

    @Query(
            value = """
                    select j as job,
                           count(application.id) as applicationCount
                    from Job j
                    left join Application application on application.jobId = j
                    where j.recruiterId.id = :recruiterId
                      and (:status is null or j.status = :status)
                    group by j
                    """,
            countQuery = """
                    select count(job)
                    from Job job
                    where job.recruiterId.id = :recruiterId
                      and (:status is null or job.status = :status)
                    """
    )
    Page<JobWithApplicationCountProjection> findByRecruiterIdWithApplicationCount(
            @Param("recruiterId") Long recruiterId,
            @Param("status") JobStatus status,
            Pageable pageable
    );

    @Modifying
    @Query("""
            update Job job
            set job.status = :closedStatus,
                job.updatedAt = :updatedAt
            where job.status = :publishedStatus
              and job.deadline < :today
              and job.isDeleted = false
            """)
    int closeExpiredPublishedJobs(@Param("publishedStatus") JobStatus publishedStatus,
                                  @Param("closedStatus") JobStatus closedStatus,
                                  @Param("today") LocalDate today,
                                  @Param("updatedAt") LocalDateTime updatedAt);
}
