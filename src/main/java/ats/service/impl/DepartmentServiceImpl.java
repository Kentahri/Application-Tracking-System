package ats.service.impl;

import ats.dto.department.DepartmentRequest;
import ats.dto.department.DepartmentResponse;
import ats.dto.department.DepartmentUpdateRequest;
import ats.entity.Department;
import ats.exception.BadRequestException;
import ats.exception.NotFoundException;
import ats.helper.MessageHelper;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import ats.mapper.DepartmentMapper;
import ats.repository.DepartmentRepository;
import ats.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    private String message(String code, Object... args) {
        return MessageHelper.getMessage(code, args);
    }

    private Department getDepartmentOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Department not found with id: {}", id);
                    return new NotFoundException(message("error.department.notFound", id));
                });
    }

    @Override
    public PageResponse<DepartmentResponse> getAllDepartments(PagingRequest pagingRequest) {
        log.debug("getting departments page: {}, size: {}", pagingRequest.getPage(), pagingRequest.getSize());

        Page<Department> departments = departmentRepository.findAll(
                pagingRequest.toPageable(Sort.by(Sort.Direction.DESC, "id"))
        );
        Page<DepartmentResponse> responses = departments.map(departmentMapper::toDto);
        return PageResponse.of(responses);
    }

    @Override
    public DepartmentResponse getDepartmentById(Long id) {
        log.debug("getting department by id: {}", id);

        Department department = getDepartmentOrThrow(id);
        return departmentMapper.toDto(department);
    }

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        log.info("creating new department with name: {}", request.getDepartmentName());

        String departmentName = request.getDepartmentName().trim();

        if (departmentRepository.existsByDepartmentName(departmentName)) {
            log.warn("Department name already exists: {}", departmentName);
            throw new BadRequestException(message("error.department.name.exists"));
        }

        request.setDepartmentName(departmentName);

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

        if (request.getDepartmentName() != null) {
            String departmentName = request.getDepartmentName().trim();
            if (departmentName.isBlank()) {
                throw new BadRequestException(message("error.department.name.blank"));
            }
            if (departmentRepository.existsByDepartmentNameAndIdNot(departmentName, id)) {
                log.warn("Department name already exists: {}", departmentName);
                throw new BadRequestException(message("error.department.name.exists"));
            }
            request.setDepartmentName(departmentName);
        }

        departmentMapper.updateEntity(request, department);

        log.info("updated department id: {} with data: {}", id, request);
        return departmentMapper.toDto(department);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("deleting department with id: {}", id);

        Department department = getDepartmentOrThrow(id);
        departmentRepository.delete(department);
        log.info("deleted department with id: {}", id);
    }
}
