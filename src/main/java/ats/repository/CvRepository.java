package ats.repository;

import ats.entity.Cv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CvRepository extends JpaRepository<Cv, Long> {

    Cv findByFilePath(String filePath);

    Optional<Cv> findByIdAndCandidateId_Id(Long id, Long candidateId);

    List<Cv> findTop5ByCandidateId_IdOrderByCreatedAtDesc(Long candidateId);
}
