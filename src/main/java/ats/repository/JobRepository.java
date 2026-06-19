package ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ats.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobRepository extends JpaRepository<Job, Long> {

    boolean existsByTitle(String title);

    boolean existsByTitleAndIdNot(String title, Long id);

    Job findByTitle(String title);

    Page<Job> findByRecruiterId_Id(Long recruiterId, Pageable pageable);
}

