package ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ats.entity.Job;

public interface JobRepository extends JpaRepository<Job, Long> {

    boolean existsByTitle(String title);
}

