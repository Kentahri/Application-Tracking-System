package ats.repository;

import ats.entity.Cv;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvRepository extends JpaRepository<Cv, Long> {

    Cv findByFilePath(String filePath);
}
