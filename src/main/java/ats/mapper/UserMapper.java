package ats.mapper;

import ats.dto.user.UserRequest;
import ats.dto.user.UserResponse;
import ats.dto.user.UserUpdateRequest;
import ats.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "departmentId", source = "departmentId.id")
    @Mapping(target = "departmentName", source = "departmentId.departmentName")
    UserResponse toDto(User user);

    List<UserResponse> toDto(List<User> users);

    @Mapping(target = "departmentId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(UserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "departmentId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget User user);
}
