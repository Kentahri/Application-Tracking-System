package ats.controller;

import ats.dto.department.DepartmentDeleteRequest;
import ats.dto.department.DepartmentRequest;
import ats.dto.department.DepartmentResponse;
import ats.dto.department.DepartmentUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ats.service.DepartmentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/departments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Departments", description = "APIs for admin department management")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @Operation(summary = "Get all departments", description = "Get all departments")
    @ApiResponse(responseCode = "200", description = "Departments retrieved successfully")
    public List<DepartmentResponse> getAll() {
        log.debug("REST request to get all departments");
        return departmentService.getAllDepartments();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by id", description = "Get department detail by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public DepartmentResponse getById(@Parameter(description = "Department id") @PathVariable Long id) {
        log.debug("REST request to get department by id: {}", id);
        return departmentService.getDepartmentById(id);
    }

    @PostMapping
    @Operation(summary = "Create department", description = "Create a new department")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public DepartmentResponse create(@RequestBody DepartmentRequest request) {
        log.debug("REST request to create department: {}", request);
        return departmentService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update department", description = "Update an existing department by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public DepartmentResponse update(@Parameter(description = "Department id") @PathVariable Long id,
                                     @RequestBody DepartmentUpdateRequest request) {
        log.debug("REST request to update department id: {}", id);
        return departmentService.update(id, request);
    }

    @DeleteMapping
    @Operation(summary = "Delete department", description = "Soft delete a department")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public void delete(@RequestBody DepartmentDeleteRequest request) {
        log.debug("REST request to delete department: {}", request.getId());
        departmentService.delete(request);
    }
}
