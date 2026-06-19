package ats.repository;

import ats.entity.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineStageRepository extends JpaRepository<PipelineStage, Long> {

    boolean existsByStageName(String stageName);

    PipelineStage findByStageName(String stageName);
}

