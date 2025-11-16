package org.example.wealthflow.asset.mappers;

import org.example.wealthflow.asset.dtos.AssetRequestDto;
import org.example.wealthflow.asset.dtos.AssetResponseDto;
import org.example.wealthflow.asset.models.Asset;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetMapper {

    AssetResponseDto toResponse(Asset asset);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastPrice", ignore = true)
    @Mapping(target = "lastPriceAt", ignore = true)
    Asset toEntity(AssetRequestDto assetRequestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastPrice", ignore = true)
    @Mapping(target = "lastPriceAt", ignore = true)
    void updateFromDto(AssetRequestDto assetRequestDto, @MappingTarget Asset asset);
}
