package ats.repository;

import ats.constant.UserRole;
import ats.constant.UserStatus;
import ats.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("""
            select u
            from User u
            where (:keyword is null or :keyword = '' or
                   lower(u.name) like lower(concat('%', :keyword, '%')) or
                   lower(u.email) like lower(concat('%', :keyword, '%')) or
                   lower(u.phone) like lower(concat('%', :keyword, '%')))
              and (:status is null or u.status = :status)
              and (:role is null or u.role = :role)
            """)
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("status") UserStatus status,
                           @Param("role") UserRole role,
                           Pageable pageable);

    List<User> findByRoleAndStatusAndDepartmentId_IdOrderByNameAsc(UserRole role, UserStatus status, Long departmentId);
}
