package ats.repository;

import ats.entity.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PipelineStageRepository extends JpaRepository<PipelineStage, Long> {

    boolean existsByStageName(String stageName);

    boolean existsByStageNameAndIdNot(String stageName, Long id);

    boolean existsByStageOrder(Integer stageOrder);

    boolean existsByStageOrderAndIdNot(Integer stageOrder, Long id);

    PipelineStage findByStageName(String stageName);

    List<PipelineStage> findAllByOrderByStageOrderAsc();

    List<PipelineStage> findAllByStageNameInOrderByStageOrderAsc(List<String> stageNames);
}

