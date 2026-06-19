package ats.repository;

import ats.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    boolean existsByApplicationId_Id(Long applicationId);
}
