package ats.controller;

import ats.constant.UserRole;
import ats.constant.UserStatus;
import ats.dto.user.UserRequest;
import ats.dto.user.UserResponse;
import ats.dto.user.UserUpdateRequest;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import ats.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "APIs for admin user management")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Get paginated users with keyword, status, and role filters")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    public PageResponse<UserResponse> getAll(@Parameter(description = "Page index, starting from 0")
                                             @RequestParam(defaultValue = "0") int page,
                                             @Parameter(description = "Number of records per page")
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) UserStatus status,
                                             @RequestParam(required = false) UserRole role) {
        log.debug("REST request to get users page: {}, size: {}, keyword: {}, status: {}, role: {}",
                page, size, keyword, status, role);
        return userService.getAllUsers(new PagingRequest(page, size), keyword, status, role);
    }

    @GetMapping("/roles")
    @Operation(summary = "Get user roles", description = "Get available user roles for dropdowns")
    public List<UserRole> getRoles() {
        return userService.getRoles();
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get user statuses", description = "Get available user statuses for dropdowns")
    public List<UserStatus> getStatuses() {
        return userService.getStatuses();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id", description = "Get user detail by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public UserResponse getById(@Parameter(description = "User id") @PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public UserResponse update(@Parameter(description = "User id") @PathVariable Long id,
                               @Valid @RequestBody UserUpdateRequest request) {
        return userService.update(id, request);
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle user status", description = "Toggle user status between ACTIVE and INACTIVE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User status toggled successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public UserResponse toggleStatus(@Parameter(description = "User id") @PathVariable Long id) {
        return userService.toggleStatus(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Soft delete a user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public void delete(@Parameter(description = "User id") @PathVariable Long id) {
        userService.delete(id);
    }
}
