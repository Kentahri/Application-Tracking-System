package quan.ats.service;

import quan.ats.dto.department.DepartmentDeleteRequest;
import quan.ats.dto.department.DepartmentRequest;
import quan.ats.dto.department.DepartmentResponse;
import quan.ats.dto.department.DepartmentUpdateRequest;

import java.util.List;

public interface DepartmentService {

    List<DepartmentResponse> getAllDepartments();

    DepartmentResponse getDepartmentById(Long id);

    DepartmentResponse create(DepartmentRequest request);

    DepartmentResponse update(Long id, DepartmentUpdateRequest request);

    void delete(DepartmentDeleteRequest request);
}
