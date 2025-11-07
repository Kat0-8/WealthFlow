package org.example.wealthflow.asset.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetRequestDto {
    @NotBlank(message = "Ticker symbol is required")
    @Size(max = 50)
    private String tickerSymbol;

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @NotNull(message = "Type is required")
    private AssetTypeDto type;

    @Size(max = 255)
    private String externalId;

    @Size(max = 100)
    private String source;

    @Size(max = 10)
    private String currency;
}