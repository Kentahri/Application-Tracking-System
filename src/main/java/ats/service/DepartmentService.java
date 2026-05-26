package ats.service;

import ats.dto.department.DepartmentDeleteRequest;
import ats.dto.department.DepartmentRequest;
import ats.dto.department.DepartmentResponse;
import ats.dto.department.DepartmentUpdateRequest;

import java.util.List;

public interface DepartmentService {

    List<DepartmentResponse> getAllDepartments();

    DepartmentResponse getDepartmentById(Long id);

    DepartmentResponse create(DepartmentRequest request);

    DepartmentResponse update(Long id, DepartmentUpdateRequest request);

    void delete(DepartmentDeleteRequest request);
}
