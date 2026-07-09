package ats.service;

import ats.constant.UserRole;
import ats.constant.UserStatus;
import ats.dto.user.UserRequest;
import ats.dto.user.UserResponse;
import ats.dto.user.UserUpdateRequest;
import ats.http.PageResponse;
import ats.http.PagingRequest;

import java.util.List;

public interface UserService {

    PageResponse<UserResponse> getAllUsers(PagingRequest pagingRequest, String keyword, UserStatus status, UserRole role);

    UserResponse getUserById(Long id);

    UserResponse create(UserRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    UserResponse toggleStatus(Long id);

    void delete(Long id);

    List<UserRole> getRoles();

    List<UserStatus> getStatuses();
}
