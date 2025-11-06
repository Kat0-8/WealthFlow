package org.example.wealthflow.mappers;

import org.example.wealthflow.dtos.asset.AssetRequestDto;
import org.example.wealthflow.dtos.asset.AssetResponseDto;
import org.example.wealthflow.models.Asset;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AssetMapper {

    public abstract AssetResponseDto toResponse(Asset asset);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastPrice", ignore = true)
    @Mapping(target = "lastPriceAt", ignore = true)
    public abstract Asset toEntity(AssetRequestDto assetRequestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastPrice", ignore = true)
    @Mapping(target = "lastPriceAt", ignore = true)
    public abstract void updateFromDto(AssetRequestDto assetRequestDto, @MappingTarget Asset asset);
}
