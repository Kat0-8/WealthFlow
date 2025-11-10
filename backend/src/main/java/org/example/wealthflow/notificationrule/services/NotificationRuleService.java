package org.example.wealthflow.notificationrule.services;

import lombok.RequiredArgsConstructor;
import org.example.wealthflow.asset.repositories.AssetRepository;
import org.example.wealthflow.common.dtos.PagedResultDto;
import org.example.wealthflow.common.exceptions.NotFoundException;
import org.example.wealthflow.notificationrule.dtos.NotificationRuleRequestDto;
import org.example.wealthflow.notificationrule.dtos.NotificationRuleResponseDto;
import org.example.wealthflow.notificationrule.mappers.NotificationRuleMapper;
import org.example.wealthflow.notificationrule.models.NotificationRule;
import org.example.wealthflow.notificationrule.repositories.NotificationRuleRepository;
import org.example.wealthflow.user.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationRuleService {

    private final NotificationRuleRepository notificationRuleRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final NotificationRuleMapper mapper;

    /* CREATE */

    public NotificationRuleResponseDto create(NotificationRuleRequestDto dto) {
        var user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found: " + dto.getUserId()));

        var asset = assetRepository.findById(dto.getAssetId())
                .orElseThrow(() -> new NotFoundException("Asset not found: " + dto.getAssetId()));

        NotificationRule entity = mapper.toEntity(dto);
        entity.setUser(user);
        entity.setAsset(asset);

        if (entity.getTargetPrice() == null) {
            throw new IllegalArgumentException("Target price required");
        }
        if (entity.getCreatedAt() == null) entity.setCreatedAt(java.time.Instant.now());

        NotificationRule saved = notificationRuleRepository.save(entity);
        return mapper.toResponse(saved);
    }

    /* READ */

    @Transactional(readOnly = true)
    public PagedResultDto<NotificationRuleResponseDto> getForUser(Long userId, int limit, int offset) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        PagedResultDto<NotificationRule> page = notificationRuleRepository.findByUserIdOrderByCreatedAtDesc(userId, limit, offset);
        List<NotificationRuleResponseDto> dtos = page
                .getItems().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return PagedResultDto.<NotificationRuleResponseDto>builder()
                .items(dtos)
                .total(page.getTotal())
                .page(page.getPage())
                .size(page.getSize())
                .build();
    }

    @Transactional(readOnly = true)
    public NotificationRuleResponseDto getById(Long id) {
        NotificationRule nr = notificationRuleRepository.findById(id).orElseThrow(() -> new NotFoundException("NotificationRule not found: " + id));
        return mapper.toResponse(nr);
    }

    /* UPDATE */

    public NotificationRuleResponseDto update(Long id, NotificationRuleRequestDto dto) {
        NotificationRule existing = notificationRuleRepository.findById(id).orElseThrow(() -> new NotFoundException("NotificationRule not found: " + id));

        if (dto.getUserId() != null && !dto.getUserId().equals(existing.getUser().getId())) {
            var user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new NotFoundException("User not found: " + dto.getUserId()));
            existing.setUser(user);
        }

        if (dto.getAssetId() != null && !dto.getAssetId().equals(existing.getAsset().getId())) {
            var asset = assetRepository.findById(dto.getAssetId())
                    .orElseThrow(() -> new NotFoundException("Asset not found: " + dto.getAssetId()));
            existing.setAsset(asset);
        }

        mapper.updateFromDto(dto, existing);

        NotificationRule saved = notificationRuleRepository.save(existing);
        return mapper.toResponse(saved);
    }

    /* DELETE */

    public void delete(Long id) {
        boolean deleted = notificationRuleRepository.deleteById(id);
        if (!deleted) throw new NotFoundException("NotificationRule not found: " + id);
    }

    @Transactional(readOnly = true)
    public List<NotificationRuleResponseDto> findEnabledByAsset(Long assetId) {
        List<NotificationRule> rules = notificationRuleRepository.findByAssetIdEnabled(assetId);
        return rules.stream().map(mapper::toResponse).collect(Collectors.toList());
    }
}

