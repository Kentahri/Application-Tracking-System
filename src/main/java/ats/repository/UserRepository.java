package ats.repository;

import ats.constant.UserRole;
import ats.constant.UserStatus;
import ats.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    List<User> findByRoleAndStatusAndDepartmentId_IdOrderByNameAsc(UserRole role, UserStatus status, Long departmentId);
}
