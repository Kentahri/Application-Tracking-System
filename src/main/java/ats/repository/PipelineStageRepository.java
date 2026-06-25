package ats.repository;

import ats.entity.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PipelineStageRepository extends JpaRepository<PipelineStage, Long> {

    boolean existsByStageName(String stageName);

    PipelineStage findByStageName(String stageName);

    List<PipelineStage> findAllByOrderByStageOrderAsc();
}

