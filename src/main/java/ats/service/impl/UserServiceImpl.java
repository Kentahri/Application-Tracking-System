package ats.service.impl;

import ats.constant.UserRole;
import ats.constant.UserStatus;
import ats.dto.user.UserRequest;
import ats.dto.user.UserResponse;
import ats.dto.user.UserUpdateRequest;
import ats.entity.Department;
import ats.entity.User;
import ats.exception.BadRequestException;
import ats.exception.NotFoundException;
import ats.helper.MessageHelper;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import ats.mapper.UserMapper;
import ats.repository.DepartmentRepository;
import ats.repository.UserRepository;
import ats.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private String message(String code, Object... args) {
        return MessageHelper.getMessage(code, args);
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new NotFoundException(message("error.user.notFound", id));
                });
    }

    private Department getDepartmentOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Department not found with id: {}", id);
                    return new NotFoundException(message("error.department.notFound", id));
                });
    }

    @Override
    public PageResponse<UserResponse> getAllUsers(PagingRequest pagingRequest,
                                                  String keyword,
                                                  UserStatus status,
                                                  UserRole role) {
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        return PageResponse.of(
                userRepository.searchUsers(normalizedKeyword, status, role, pagingRequest.toPageable())
                        .map(userMapper::toDto)
        );
    }

    @Override
    public UserResponse getUserById(Long id) {
        return userMapper.toDto(getUserOrThrow(id));
    }

    @Override
    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("User email already exists: {}", request.getEmail());
            throw new BadRequestException(message("error.user.email.exists"));
        }

        User user = userMapper.toEntity(request);
        user.setDepartmentId(resolveDepartment(request.getDepartmentId()));
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);
        log.info("created user with id: {}", saved.getId());
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getUserOrThrow(id);

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = getDepartmentOrThrow(request.getDepartmentId());
        }
        if (request.getEmail() != null) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                log.warn("User email already exists: {}", request.getEmail());
                throw new BadRequestException(message("error.user.email.exists"));
            }
        }
        if (request.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        userMapper.updateEntity(request, user);
        if (department != null) {
            user.setDepartmentId(department);
        }

        log.info("updated user with id: {}", id);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponse toggleStatus(Long id) {
        User user = getUserOrThrow(id);
        user.setStatus(user.getStatus() == UserStatus.ACTIVE ? UserStatus.INACTIVE : UserStatus.ACTIVE);
        log.info("toggled user status id: {} to {}", id, user.getStatus());
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = getUserOrThrow(id);
        userRepository.delete(user);
        log.info("deleted user with id: {}", id);
    }

    @Override
    public List<UserRole> getRoles() {
        return Arrays.asList(UserRole.values());
    }

    @Override
    public List<UserStatus> getStatuses() {
        return Arrays.asList(UserStatus.values());
    }

    private Department resolveDepartment(Long departmentId) {
        return departmentId == null ? null : getDepartmentOrThrow(departmentId);
    }
}
