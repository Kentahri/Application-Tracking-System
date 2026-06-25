package ats.service;

import ats.dto.department.DepartmentRequest;
import ats.dto.department.DepartmentResponse;
import ats.dto.department.DepartmentUpdateRequest;
import ats.http.PageResponse;
import ats.http.PagingRequest;

public interface DepartmentService {

    PageResponse<DepartmentResponse> getAllDepartments(PagingRequest pagingRequest);

    DepartmentResponse getDepartmentById(Long id);

    DepartmentResponse create(DepartmentRequest request);

    DepartmentResponse update(Long id, DepartmentUpdateRequest request);

    void delete(Long id);
}
