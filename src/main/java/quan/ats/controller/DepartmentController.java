package quan.ats.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import quan.ats.dto.department.*;
import quan.ats.service.DepartmentService;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Slf4j
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public List<DepartmentResponse> getAll() {
        log.debug("REST request to get all departments");
        return departmentService.getAllDepartments();
    }

    @GetMapping("/{id}")
    public DepartmentResponse getById(@PathVariable Long id) {
        log.debug("REST request to get department by id: {}", id);
        return departmentService.getDepartmentById(id);
    }

    @PostMapping
    public DepartmentResponse create(@RequestBody DepartmentRequest request) {
        log.debug("REST request to create department: {}", request);
        return departmentService.create(request);
    }

    @PutMapping("/{id}")
    public DepartmentResponse update(@PathVariable Long id,
                                     @RequestBody DepartmentUpdateRequest request) {
        log.debug("REST request to update department id: {}", id);
        return departmentService.update(id, request);
    }

    @DeleteMapping
    public void delete(@RequestBody DepartmentDeleteRequest request) {
        log.debug("REST request to delete department: {}", request.getId());
        departmentService.delete(request);
    }
}