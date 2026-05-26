package quan.ats.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quan.ats.dto.department.DepartmentDeleteRequest;
import quan.ats.dto.department.DepartmentRequest;
import quan.ats.dto.department.DepartmentResponse;
import quan.ats.dto.department.DepartmentUpdateRequest;
import quan.ats.entity.Department;
import quan.ats.mapper.DepartmentMapper;
import quan.ats.repository.DepartmentRepository;
import quan.ats.service.DepartmentService;

import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    private Department getDepartmentOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Department not found with id: {}", id);
                    return new RuntimeException("Không tìm thấy phòng ban với id: " + id);
                });
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        log.debug("getting all departments");
        List<Department> department = departmentRepository.findAll();
        List<DepartmentResponse> responses = departmentMapper.toDto(department);
        return responses;
    }

    @Override
    public DepartmentResponse getDepartmentById(Long id) {
        log.debug("getting department by id: {}", id);

        Department department = getDepartmentOrThrow(id);
        DepartmentResponse response = departmentMapper.toDto(department);
        return response;
    }

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        log.info("creating new department with name: {}", request.getDepartmentName());

        if(departmentRepository.existsByDepartmentName(request.getDepartmentName())) {
            log.warn("Department name already exists: {}", request.getDepartmentName());
            throw new RuntimeException("Tên phòng ban đã tồn tại");
        }

        Department department = departmentMapper.toEntity(request);
        Department saved = departmentRepository.save(department);

        log.info("created department with id: {}", saved.getId());
        return departmentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentUpdateRequest request) {
        log.info("updating department with id: {}", id);

        Department department = getDepartmentOrThrow(id);

        departmentMapper.updateEntity(request, department);

        log.info("updated department id: {} with data: {}", id, request);
        return departmentMapper.toDto(department);
    }

    @Override
    public void delete(DepartmentDeleteRequest request) {
        log.info("deleting department with id: {}", request.getId());

        Department department = getDepartmentOrThrow(request.getId());
        departmentRepository.delete(department);
        log.info("deleted department with id: {}", request.getId());
    }
}
