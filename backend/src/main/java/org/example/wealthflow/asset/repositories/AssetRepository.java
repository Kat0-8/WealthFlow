package org.example.wealthflow.asset.repositories;

import lombok.RequiredArgsConstructor;
import org.example.wealthflow.common.dtos.PagedResultDto;
import org.example.wealthflow.asset.models.Asset;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertResultStep;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import org.jooq.Record;

@Repository
@RequiredArgsConstructor
public class AssetRepository {

    private final DSLContext dslContext;

    private final Table<?> ASSETS = table("assets");
    private final Field<Long> ID = field("id", Long.class);
    private final Field<String> TICKER = field("ticker_symbol", String.class);
    private final Field<String> NAME = field("name", String.class);
    private final Field<String> TYPE = field("type", String.class);
    private final Field<String> EXTERNAL_ID = field("external_id", String.class);
    private final Field<String> SOURCE = field("source", String.class);
    private final Field<String> CURRENCY = field("currency", String.class);
    private final Field<BigDecimal> LAST_PRICE = field("last_price", BigDecimal.class);
    private final Field<Instant> LAST_PRICE_AT = field("last_price_at", Instant.class);
    private final Field<Instant> CREATED_AT = field("created_at", Instant.class);
    private final Field<Instant> UPDATED_AT = field("updated_at", Instant.class);

    public Optional<Asset> findById(Long id) {
        return dslContext.selectFrom(ASSETS)
                .where(ID.eq(id))
                .fetchOptional(this::mapRecordToAsset);
    }

    public Optional<Asset> findByTicker(String tickerSymbol) {
        return dslContext.selectFrom(ASSETS)
                .where(TICKER.eq(tickerSymbol))
                .fetchOptional(this::mapRecordToAsset);
    }

    public Optional<Asset> findByName(String name) {
        return dslContext.selectFrom(ASSETS)
                .where(NAME.eq(name))
                .fetchOptional(this::mapRecordToAsset);
    }

    public Optional<Asset> findByExternalId(String externalId) {
        return dslContext.selectFrom(ASSETS)
                .where(EXTERNAL_ID.eq(externalId))
                .fetchOptional(this::mapRecordToAsset);
    }

    public PagedResultDto<Asset> searchWithTotal(String q, int limit, int offset) {
        Condition cond = buildSearchCondition(q);

        Field<Integer> totalField = DSL.count().over().as("total_count");

        Result<Record> result = dslContext.select(ASSETS.fields())
                .select(totalField)
                .from(ASSETS)
                .where(cond)
                .orderBy(TICKER.asc())
                .limit(limit)
                .offset(offset)
                .fetch();

        List<Asset> items = new ArrayList<>();
        long total = 0;
        for (Record r : result) {
            items.add(mapRecordToAsset(r));
            if (total == 0 && r.field("total_count") != null) {
                Number n = r.get("total_count", Number.class);
                if (n != null) total = n.longValue();
            }
        }

        return PagedResultDto.<Asset>builder()
                .items(items)
                .total(total)
                .page(offset / Math.max(1, limit))
                .size(limit)
                .build();
    }

    public Asset save(Asset asset) {
        if (asset.getId() == null) {
            return insert(asset);
        } else {
            update(asset);
            return asset;
        }
    }

    public Asset createIfNotExistsByTicker(Asset candidate) {
        if (candidate == null || candidate.getTickerSymbol() == null) {
            throw new IllegalArgumentException("candidate and ticker required");
        }

        String ticker = candidate.getTickerSymbol().trim();

        try {
            var insert = dslContext.insertInto(ASSETS)
                    .set(TICKER, ticker)
                    .set(NAME, candidate.getName())
                    .set(TYPE, candidate.getType() == null ? null : candidate.getType().name())
                    .set(EXTERNAL_ID, candidate.getExternalId())
                    .set(SOURCE, candidate.getSource())
                    .set(CURRENCY, candidate.getCurrency())
                    .set(LAST_PRICE, candidate.getLastPrice())
                    .set(LAST_PRICE_AT, candidate.getLastPriceAt())
                    .set(CREATED_AT, candidate.getCreatedAt() == null ? Instant.now() : candidate.getCreatedAt())
                    .set(UPDATED_AT, candidate.getUpdatedAt() == null ? Instant.now() : candidate.getUpdatedAt());

            InsertResultStep<?> step = insert.onConflict(TICKER).doNothing().returning(ID);
            Long newId = step.fetchOne(ID);

            if (newId != null) {
                candidate.setId(newId);
                return candidate;
            }
            return findByTicker(ticker).orElseThrow(() ->
                    new DataAccessException("Failed to create or find existing asset for ticker " + ticker));
        } catch (DataAccessException ex) {
            return findByTicker(ticker).orElseThrow(() -> ex);
        }
    }

    private Asset insert(Asset asset) {
        Long id = dslContext.insertInto(ASSETS)
                .set(TICKER, asset.getTickerSymbol())
                .set(NAME, asset.getName())
                .set(TYPE, asset.getType() == null ? null : asset.getType().name())
                .set(EXTERNAL_ID, asset.getExternalId())
                .set(SOURCE, asset.getSource())
                .set(CURRENCY, asset.getCurrency())
                .set(LAST_PRICE, asset.getLastPrice())
                .set(LAST_PRICE_AT, asset.getLastPriceAt())
                .set(CREATED_AT, asset.getCreatedAt() == null ? Instant.now() : asset.getCreatedAt())
                .set(UPDATED_AT, asset.getUpdatedAt() == null ? Instant.now() : asset.getUpdatedAt())
                .returning(ID)
                .fetchOne(ID);
        asset.setId(id);
        return asset;
    }

    private void update(Asset asset) {
        dslContext.update(ASSETS)
                .set(TICKER, asset.getTickerSymbol())
                .set(NAME, asset.getName())
                .set(TYPE, asset.getType() == null ? null : asset.getType().name())
                .set(EXTERNAL_ID, asset.getExternalId())
                .set(SOURCE, asset.getSource())
                .set(CURRENCY, asset.getCurrency())
                .set(LAST_PRICE, asset.getLastPrice())
                .set(LAST_PRICE_AT, asset.getLastPriceAt())
                .set(UPDATED_AT, Instant.now())
                .where(ID.eq(asset.getId()))
                .execute();
    }

    public boolean updateLastPriceById(Long id, BigDecimal price, Instant lastPriceAt) {
        int updatedRows = dslContext.update(ASSETS)
                .set(LAST_PRICE, price)
                .set(LAST_PRICE_AT, lastPriceAt)
                .set(UPDATED_AT, Instant.now())
                .where(ID.eq(id))
                .execute();
        return updatedRows > 0;
    }

    public boolean deleteById(Long id) {
        int deletedRows = dslContext.deleteFrom(ASSETS)
                .where(ID.eq(id))
                .execute();
        return deletedRows > 0;
    }

    public boolean delete(Asset asser) {
        if(asser.getId() == null) {
            return false;
        } else {
            return deleteById(asser.getId());
        }
    }

    private Asset mapRecordToAsset(Record record) {
        if (record == null) return null;
        Asset asset = new Asset();
        asset.setId(record.get(ID));
        asset.setTickerSymbol(record.get(TICKER));
        asset.setName(record.get(NAME));
        String type = record.get(TYPE);
        asset.setType(type == null ? null : Asset.Type.valueOf(type));
        asset.setExternalId(record.get(EXTERNAL_ID));
        asset.setSource(record.get(SOURCE));
        asset.setCurrency(record.get(CURRENCY));
        asset.setLastPrice(record.get(LAST_PRICE));
        asset.setLastPriceAt(record.get(LAST_PRICE_AT));
        asset.setCreatedAt(record.get(CREATED_AT));
        asset.setUpdatedAt(record.get(UPDATED_AT));
        return asset;
    }

    private Condition buildSearchCondition(String q) {
        Condition cond = DSL.trueCondition();
        if (q != null && !q.isBlank()) {
            String pattern = "%" + q.trim().toLowerCase() + "%";
            cond = DSL.lower(TICKER).like(pattern)
                    .or(DSL.lower(NAME).like(pattern))
                    .or(DSL.lower(EXTERNAL_ID).like(pattern));
        }
        return cond;
    }
}
