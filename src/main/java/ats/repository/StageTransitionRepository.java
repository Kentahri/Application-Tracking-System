package ats.repository;

import ats.entity.StageTransition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StageTransitionRepository extends JpaRepository<StageTransition, Long> {

    boolean existsByApplicationId_IdAndToStageId_Id(Long applicationId, Long toStageId);
}
