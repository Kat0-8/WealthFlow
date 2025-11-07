package org.example.wealthflow.asset.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.wealthflow.asset.dtos.AssetRequestDto;
import org.example.wealthflow.asset.dtos.AssetResponseDto;
import org.example.wealthflow.common.dtos.PagedResultDto;
import org.example.wealthflow.common.exceptions.AlreadyExistsException;
import org.example.wealthflow.common.exceptions.BadRequestException;
import org.example.wealthflow.common.exceptions.NotFoundException;
import org.example.wealthflow.asset.mappers.AssetMapper;
import org.example.wealthflow.asset.models.Asset;
import org.example.wealthflow.asset.repositories.AssetRepository;
import org.jooq.exception.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 200;

    /* CREATE */

    @Transactional
    public AssetResponseDto create(AssetRequestDto dto) {
        if (dto == null) throw new BadRequestException("Asset data is required");

        String ticker = dto.getTickerSymbol().trim();
        String externalId = dto.getExternalId() == null ? null : dto.getExternalId().trim();

        if (assetRepository.findByTicker(ticker).isPresent()) {
            throw new AlreadyExistsException("Asset with ticker '" + ticker + "' already exists");
        }

        if (externalId != null && assetRepository.findByExternalId(externalId).isPresent()) {
            throw new AlreadyExistsException("Asset with externalId '" + externalId + "' already exists");
        }

        Asset candidate = assetMapper.toEntity(dto);
        candidate.setTickerSymbol(ticker);
        if (candidate.getType() == null) {
            throw new BadRequestException("Asset type is invalid");
        }

        Instant now = Instant.now();
        candidate.setCreatedAt(now);
        candidate.setUpdatedAt(now);

        try {
            Asset created = assetRepository.createIfNotExistsByTicker(candidate);
            log.info("Asset created (or taken existing) id={}, ticker={}", created.getId(), created.getTickerSymbol());
            return assetMapper.toResponse(created);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Data integrity violation creating asset ticker={}: {}", ticker, ex.getMessage());
            throw new AlreadyExistsException("Asset with ticker '" + ticker + "' already exists");
        } catch (DataAccessException ex) {
            log.error("DB error creating asset ticker={}: {}", ticker, ex.getMessage());
            throw new BadRequestException("Database error while creating asset");
        } catch (RuntimeException ex) {
            log.error("Unexpected error creating asset ticker={}: {}", ticker, ex.getMessage());
            throw new BadRequestException("Failed to create asset");
        }
    }

    /* READ */

    @Transactional(readOnly = true)
    public Asset loadById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Asset not found with id=" + id));
    }

    @Transactional(readOnly = true)
    public AssetResponseDto getById(Long id) {
        Asset asset = loadById(id);
        return assetMapper.toResponse(asset);
    }

    @Transactional(readOnly = true)
    public AssetResponseDto getByTicker(String ticker) {
        if (!StringUtils.hasText(ticker)) throw new BadRequestException("Ticker is required");
        Asset asset = assetRepository.findByTicker(ticker.trim())
                .orElseThrow(() -> new NotFoundException("Asset not found with ticker " + ticker));
        return assetMapper.toResponse(asset);
    }

    /* UPDATE */

    @Transactional
    public AssetResponseDto update(Long id, AssetRequestDto dto) {
        if (id == null) throw new BadRequestException("Asset id required");
        if (dto == null) throw new BadRequestException("Payload required");

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Asset not found with id " + id));

        if (StringUtils.hasText(dto.getExternalId()) &&
                (asset.getExternalId() == null || !asset.getExternalId().equals(dto.getExternalId()))) {
            assetRepository.findByExternalId(dto.getExternalId().trim()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new AlreadyExistsException("Asset with externalId '" + dto.getExternalId() + "' already exists");
                }
            });
        }

        dto.setTickerSymbol(null);

        if (dto.getType() != null) {
            try {
                Asset.Type.valueOf(dto.getType().name());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Type value must be one of " + String.join(", ", allowedTypes()));
            }
        }

        assetMapper.updateFromDto(dto, asset);
        asset.setUpdatedAt(Instant.now());

        try {
            assetRepository.save(asset);
            log.info("Asset updated id={}", asset.getId());
            return assetMapper.toResponse(asset);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Data integrity violation updating asset id={}: {}", id, ex.getMessage());
            throw new AlreadyExistsException("Asset update violates uniqueness constraints");
        } catch (DataAccessException ex) {
            log.error("DB error updating asset id={}: {}", id, ex.getMessage());
            throw new BadRequestException("Database error while updating asset");
        }
    }

    /* DELETE */

    @Transactional
    public void delete(Long id) {
        boolean ok = assetRepository.deleteById(id);
        if (!ok) throw new NotFoundException("Asset not found with id " + id);
        log.info("Asset deleted id={}", id);
    }

    /* SEARCH */

    @Transactional(readOnly = true)
    public PagedResultDto<AssetResponseDto> search(String q, Integer page, Integer size) {
        int p = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int s = (size == null || size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = p * s;

        var paged = assetRepository.searchWithTotal(q, s, offset);
        List<AssetResponseDto> dtos = paged.getItems().stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResultDto.<AssetResponseDto>builder()
                .items(dtos)
                .total(paged.getTotal())
                .page(p)
                .size(s)
                .build();
    }

    private String[] allowedTypes() {
        Asset.Type[] vals = Asset.Type.values();
        String[] out = new String[vals.length];
        for (int i = 0; i < vals.length; i++) out[i] = vals[i].name();
        return out;
    }
}
