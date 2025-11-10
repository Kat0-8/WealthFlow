package org.example.wealthflow.pricehistory.mappers;

import org.example.wealthflow.pricehistory.dtos.PriceHistoryRequestDto;
import org.example.wealthflow.pricehistory.dtos.PriceHistoryResponseDto;
import org.example.wealthflow.pricehistory.models.PriceHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PriceHistoryMapper {

    @Mapping(target = "assetId", source = "asset.id")
    public abstract PriceHistoryResponseDto toResponse(PriceHistory priceHistory);

    @Mapping(target = "asset", ignore = true)
    public abstract PriceHistory toEntity(PriceHistoryRequestDto dto);
}
