package org.example.wealthflow.pricehistory.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.wealthflow.asset.models.Asset;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "price_history", indexes = {
        @Index(columnList = "asset_id"),
        @Index(columnList = "recorded_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "price", precision = 18, scale = 8, nullable = false)
    private BigDecimal price;

    @Column(name = "source", length = 100)
    private String source;
}
