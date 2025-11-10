package org.example.wealthflow.notificationrule.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class NotificationRuleRequestDto {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Asset ID is required")
    private Long assetId;

    @NotNull(message = "Target price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Target price must be greater than 0")
    private BigDecimal targetPrice;

    @NotNull(message = "Direction is required")
    private NotificationRuleDirectionDto direction;

    private Boolean enabled;
    private Boolean repeat;
    private Instant lastTriggered;
}
