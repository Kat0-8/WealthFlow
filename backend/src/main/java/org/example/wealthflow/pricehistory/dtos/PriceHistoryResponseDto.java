package org.example.wealthflow.pricehistory.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistoryResponseDto {
    private Long id;
    private Long assetId;
    private Instant recordedAt;
    private BigDecimal price;
    private String source;
}