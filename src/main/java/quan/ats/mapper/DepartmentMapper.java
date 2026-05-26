package quan.ats.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import quan.ats.dto.department.DepartmentRequest;
import quan.ats.dto.department.DepartmentResponse;
import quan.ats.dto.department.DepartmentUpdateRequest;
import quan.ats.entity.Department;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    DepartmentResponse toDto(Department department);

    List<DepartmentResponse> toDto(List<Department> departments);

    Department toEntity(DepartmentRequest departmentRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(DepartmentUpdateRequest departmentUpdateRequest, @MappingTarget Department department);
}
