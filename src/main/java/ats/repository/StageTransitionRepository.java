package ats.repository;

import ats.entity.StageTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StageTransitionRepository extends JpaRepository<StageTransition, Long> {

    boolean existsByApplicationId_IdAndToStageId_Id(Long applicationId, Long toStageId);

    @Query("""
            select st
            from StageTransition st
            left join fetch st.fromStageId
            join fetch st.toStageId
            where st.applicationId.id = :applicationId
            order by st.movedAt asc, st.id asc
            """)
    List<StageTransition> findByApplicationIdWithStages(@Param("applicationId") Long applicationId);
}
