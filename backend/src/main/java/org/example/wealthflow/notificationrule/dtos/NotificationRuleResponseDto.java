package org.example.wealthflow.notificationrule.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRuleResponseDto {
    private Long id;
    private Long userId;
    private Long assetId;
    private BigDecimal targetPrice;
    private NotificationRuleDirectionDto direction;
    private boolean enabled;
    private boolean repeat;
    private Instant lastTriggered;
    private Instant createdAt;
}