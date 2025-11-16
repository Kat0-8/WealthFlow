package org.example.wealthflow.user.mappers;

import org.example.wealthflow.user.dtos.user.UserRequestDto;
import org.example.wealthflow.user.dtos.user.UserResponseDto;
import org.example.wealthflow.user.dtos.user.UserUpdateDto;
import org.example.wealthflow.user.models.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserResponseDto toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "salt", ignore = true)
    User toEntity(UserRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "login", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "salt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateFromDto(UserUpdateDto dto, @MappingTarget User user);

}
