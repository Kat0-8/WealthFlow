package org.example.wealthflow.pricehistory.service;

import lombok.RequiredArgsConstructor;
import org.example.wealthflow.asset.repositories.AssetRepository;
import org.example.wealthflow.common.dtos.PagedResultDto;
import org.example.wealthflow.common.exceptions.NotFoundException;
import org.example.wealthflow.pricehistory.dtos.PriceHistoryRequestDto;
import org.example.wealthflow.pricehistory.dtos.PriceHistoryResponseDto;
import org.example.wealthflow.pricehistory.mappers.PriceHistoryMapper;
import org.example.wealthflow.pricehistory.models.PriceHistory;
import org.example.wealthflow.pricehistory.repositories.PriceHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PriceHistoryService {

    private final PriceHistoryRepository repository;
    private final AssetRepository assetRepository;
    private final PriceHistoryMapper priceHistoryMapper;

    /* CREATE */

    @Transactional
    public PriceHistoryResponseDto create(PriceHistoryRequestDto dto) {
        var asset = assetRepository.findById(dto.getAssetId())
                .orElseThrow(() -> new NotFoundException("Asset not found: " + dto.getAssetId()));

        PriceHistory entity = priceHistoryMapper.toEntity(dto);
        entity.setAsset(asset);

        PriceHistory saved = repository.save(entity);
        return priceHistoryMapper.toResponse(saved);
    }

    /* READ */

    @Transactional(readOnly = true)
    public PagedResultDto<PriceHistoryResponseDto> getForAsset(Long assetId, int limit, int offset) {
        assetRepository.findById(assetId)
                .orElseThrow(() -> new NotFoundException("Asset not found: " + assetId));

        PagedResultDto<PriceHistory> page = repository.findByAssetIdOrderByRecordedAtDesc(assetId, limit, offset);
        var dtoList = page.getItems().stream().map(priceHistoryMapper::toResponse).collect(Collectors.toList());

        return PagedResultDto.<PriceHistoryResponseDto>builder()
                .items(dtoList)
                .total(page.getTotal())
                .page(page.getPage())
                .size(page.getSize())
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<PriceHistoryResponseDto> getLatestForAsset(Long assetId) {
        return repository.findTopByAssetIdOrderByRecordedAtDesc(assetId)
                .map(priceHistoryMapper::toResponse);
    }

    /* DELETE */

    @Transactional
    public void delete(Long id) {
        boolean deleted = repository.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("PriceHistory not found: " + id);
        }
    }
}
