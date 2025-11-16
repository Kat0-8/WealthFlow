package org.example.wealthflow.favourite.mappers;

import org.example.wealthflow.favourite.dtos.FavouriteResponseDto;
import org.example.wealthflow.favourite.models.Favourite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FavouriteMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "assetId", source = "asset.id")
    FavouriteResponseDto toResponse(Favourite favourite);
}