package quan.ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import quan.ats.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByDepartmentName(String departmentName);
}
