package org.example.wealthflow.mappers;

import org.example.wealthflow.dtos.user.UserRequestDto;
import org.example.wealthflow.dtos.user.UserResponseDto;
import org.example.wealthflow.dtos.user.UserUpdateDto;
import org.example.wealthflow.models.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {

    public abstract UserResponseDto toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "salt", ignore = true)
    public abstract User toEntity(UserRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "login", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "salt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    public abstract void updateFromDto(UserUpdateDto dto, @MappingTarget User user);

}
