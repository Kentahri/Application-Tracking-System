package ats.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ats.constant.JobStatus;
import ats.entity.Job;
import ats.repository.projection.JobWithApplicationCountProjection;


public interface JobRepository extends JpaRepository<Job, Long> {

    boolean existsByTitle(String title);

    boolean existsByTitleAndIdNot(String title, Long id);

    Job findByTitle(String title);

    Page<Job> findByRecruiterId_Id(Long recruiterId, Pageable pageable);

    @Query(value = "SELECT j FROM Job j JOIN FETCH j.departmentId WHERE j.status = :status",
           countQuery = "SELECT count(j) FROM Job j WHERE j.status = :status")
    Page<Job> findByStatusWithDepartment(@Param("status") JobStatus status, Pageable pageable);

    @Query("SELECT j FROM Job j JOIN FETCH j.departmentId WHERE j.status = :status ORDER BY j.id DESC")
    List<Job> findAllByStatusWithDepartment(@Param("status") JobStatus status);

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
