package ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ats.constant.JobStatus;
import ats.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobRepository extends JpaRepository<Job, Long> {

    boolean existsByTitle(String title);

    boolean existsByTitleAndIdNot(String title, Long id);

    Job findByTitle(String title);

    Page<Job> findByRecruiterId_Id(Long recruiterId, Pageable pageable);


    @Query(value = "SELECT j FROM Job j JOIN FETCH j.departmentId WHERE j.status = :status",
           countQuery = "SELECT count(j) FROM Job j WHERE j.status = :status")
    Page<Job> findByStatusWithDepartment(@Param("status") JobStatus status, Pageable pageable);
}

