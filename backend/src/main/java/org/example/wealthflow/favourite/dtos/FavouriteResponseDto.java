package org.example.wealthflow.favourite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavouriteResponseDto {
    private Long id;
    private Long userId;
    private Long assetId;
    private Instant createdAt;
}

