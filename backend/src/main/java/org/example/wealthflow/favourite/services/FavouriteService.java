package org.example.wealthflow.favourite.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.wealthflow.asset.repositories.AssetRepository;
import org.example.wealthflow.common.dtos.PagedResultDto;
import org.example.wealthflow.favourite.dtos.FavouriteRequestDto;
import org.example.wealthflow.favourite.dtos.FavouriteResponseDto;
import org.example.wealthflow.favourite.mappers.FavouriteMapper;
import org.example.wealthflow.favourite.repositories.FavouriteRepository;
import org.example.wealthflow.user.repositories.UserRepository;
import org.example.wealthflow.common.exceptions.BadRequestException;
import org.example.wealthflow.common.exceptions.NotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavouriteService {

    private final FavouriteRepository favouriteRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final FavouriteMapper favouriteMapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 200;

    /* CREATE */

    @Transactional
    public FavouriteResponseDto addFavourite(Long userId, FavouriteRequestDto request) {
        if (request == null || request.getAssetId() == null) {
            throw new BadRequestException("assetId required");
        }
        Long assetId = request.getAssetId();

        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User not found");
        }
        if (assetRepository.findById(assetId).isEmpty()) {
            throw new NotFoundException("Asset not found");
        }

        try {
            var fav = favouriteRepository.createIfNotExists(userId, assetId);
            return favouriteMapper.toResponse(fav);
        } catch (DataAccessException ex) {
            log.error("DB error creating favourite user={} asset={}: {}", userId, assetId, ex.getMessage());
            throw ex;
        }
    }

    /* READ */

    @Transactional(readOnly = true)
    public PagedResultDto<FavouriteResponseDto> listFavourites(Long userId, Integer page, Integer size) {
        if (userId == null) throw new BadRequestException("userId required");

        int p = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int s = (size == null || size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = p * s;

        var paged = favouriteRepository.findByUserIdWithTotal(userId, s, offset);
        List<FavouriteResponseDto> dtos = paged.getItems().stream()
                .map(favouriteMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResultDto.<FavouriteResponseDto>builder()
                .items(dtos)
                .total(paged.getTotal())
                .page(p)
                .size(s)
                .build();
    }

    /* DELETE */

    @Transactional
    public boolean removeFavourite(Long userId, Long assetId) {
        if (userId == null || assetId == null) throw new BadRequestException("userId and assetId required");
        return favouriteRepository.deleteByUserAndAsset(userId, assetId);
    }

    /* HELPER */

    @Transactional(readOnly = true)
    public boolean isFavourite(Long userId, Long assetId) {
        return favouriteRepository.existsByUserAndAsset(userId, assetId);
    }
}

