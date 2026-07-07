package ats.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ats.dto.job.JobRequest;
import ats.dto.job.JobResponse;
import ats.dto.job.JobUpdateRequest;
import ats.entity.Department;
import ats.entity.Job;
import ats.entity.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JobMapper {

    @Mapping(target = "departmentId", source = "departmentId.id")
    @Mapping(target = "recruiterId", source = "recruiterId.id")
    @Mapping(target = "applicationCount", ignore = true)
    JobResponse toDto(Job job);

    List<JobResponse> toDto(List<Job> jobs);

    @Mapping(target = "departmentId", source = "departmentId", qualifiedByName = "mapDepartmentIdToEntity")
    @Mapping(target = "recruiterId", ignore = true)
    Job toEntity(JobRequest jobRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "departmentId", source = "departmentId", qualifiedByName = "mapDepartmentIdToEntity")
    @Mapping(target = "recruiterId", ignore = true)
    void updateEntity(JobUpdateRequest jobUpdateRequest, @MappingTarget Job job);

    @Named("mapDepartmentIdToEntity")
    default Department mapDepartmentIdToEntity(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        Department department = new Department();
        department.setId(departmentId);
        return department;
    }

    @Named("mapUserIdToEntity")
    default User mapUserIdToEntity(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = new User();
        user.setId(userId);
        return user;
    }
}


