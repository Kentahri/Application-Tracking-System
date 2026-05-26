package ats.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ats.dto.department.DepartmentRequest;
import ats.dto.department.DepartmentResponse;
import ats.dto.department.DepartmentUpdateRequest;
import ats.entity.Department;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    DepartmentResponse toDto(Department department);

    List<DepartmentResponse> toDto(List<Department> departments);

    Department toEntity(DepartmentRequest departmentRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(DepartmentUpdateRequest departmentUpdateRequest, @MappingTarget Department department);
}
