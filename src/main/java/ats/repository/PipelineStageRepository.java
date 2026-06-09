package ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ats.entity.PinelineStage;

public interface PipelineStageRepository extends JpaRepository<PinelineStage, Long> {

    boolean existsByStageName(String stageName);
}

