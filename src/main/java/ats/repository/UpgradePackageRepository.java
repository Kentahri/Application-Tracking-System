package ats.repository;

import ats.entity.UpgradePackage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UpgradePackageRepository extends JpaRepository<UpgradePackage, Long> {

    boolean existsByPackageName(String packageName);

    boolean existsByPackageNameAndIdNot(String packageName, Long id);
}
