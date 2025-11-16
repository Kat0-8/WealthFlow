package org.example.wealthflow.notificationrule.mappers;

import org.example.wealthflow.notificationrule.dtos.NotificationRuleRequestDto;
import org.example.wealthflow.notificationrule.dtos.NotificationRuleResponseDto;
import org.example.wealthflow.notificationrule.models.NotificationRule;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationRuleMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "assetId", source = "asset.id")
    NotificationRuleResponseDto toResponse(NotificationRule entity);

    NotificationRule toEntity(NotificationRuleRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "asset", ignore = true)
    void updateFromDto(NotificationRuleRequestDto dto, @MappingTarget NotificationRule entity);

}
