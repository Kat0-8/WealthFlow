package org.example.wealthflow.dtos.asset;

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
public class AssetResponseDto {
    private Long id;
    private String tickerSymbol;
    private String name;
    private AssetTypeDto type;
    private String externalId;
    private String source;
    private String currency;
    private BigDecimal lastPrice;
    private Instant lastPriceAt;
    private Instant createdAt;
    private Instant updatedAt;
}