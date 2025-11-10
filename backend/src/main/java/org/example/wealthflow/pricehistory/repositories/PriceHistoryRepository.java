package org.example.wealthflow.pricehistory.repositories;

import lombok.RequiredArgsConstructor;
import org.example.wealthflow.asset.models.Asset;
import org.example.wealthflow.common.dtos.PagedResultDto;
import org.example.wealthflow.pricehistory.models.PriceHistory;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Repository
@RequiredArgsConstructor
public class PriceHistoryRepository {

    private final DSLContext dsl;

    private final Table<?> PRICE_HISTORY = table("price_history");
    private final Field<Long> ID = field("id", Long.class);
    private final Field<Long> ASSET_ID = field("asset_id", Long.class);
    private final Field<Instant> RECORDED_AT = field("recorded_at", Instant.class);
    private final Field<BigDecimal> PRICE = field("price", BigDecimal.class);
    private final Field<String> SOURCE = field("source", String.class);

    public PagedResultDto<PriceHistory> findByAssetIdOrderByRecordedAtDesc(Long assetId, int limit, int offset) {
        Condition cond = ASSET_ID.eq(assetId);

        Field<Integer> totalField = DSL.count().over().as("total_count");

        Result<Record> result = dsl.select(PRICE_HISTORY.fields())
                .select(totalField)
                .from(PRICE_HISTORY)
                .where(cond)
                .orderBy(RECORDED_AT.desc())
                .limit(limit)
                .offset(offset)
                .fetch();

        List<PriceHistory> items = new ArrayList<>();
        long total = 0;
        for (Record r : result) {
            items.add(mapRecordToPriceHistory(r));
            if (total == 0 && r.field("total_count") != null) {
                Number n = r.get("total_count", Number.class);
                if (n != null) total = n.longValue();
            }
        }

        return PagedResultDto.<PriceHistory>builder()
                .items(items)
                .total(total)
                .page(offset / Math.max(1, limit))
                .size(limit)
                .build();
    }

    public Optional<PriceHistory> findTopByAssetIdOrderByRecordedAtDesc(Long assetId) {
        Record r = dsl.selectFrom(PRICE_HISTORY)
                .where(ASSET_ID.eq(assetId))
                .orderBy(RECORDED_AT.desc())
                .limit(1)
                .fetchOne();
        return Optional.ofNullable(mapRecordToPriceHistory(r));
    }

    public Optional<PriceHistory> findById(Long id) {
        return dsl.selectFrom(PRICE_HISTORY)
                .where(ID.eq(id))
                .fetchOptional(this::mapRecordToPriceHistory);
    }

    public PriceHistory save(PriceHistory ph) {
        if (ph.getId() == null) {
            Long newId = dsl.insertInto(PRICE_HISTORY)
                    .set(ASSET_ID, ph.getAsset().getId())
                    .set(RECORDED_AT, ph.getRecordedAt())
                    .set(PRICE, ph.getPrice())
                    .set(SOURCE, ph.getSource())
                    .returning(ID)
                    .fetchOne(ID);
            ph.setId(newId);
            return ph;
        } else {
            dsl.update(PRICE_HISTORY)
                    .set(ASSET_ID, ph.getAsset().getId())
                    .set(RECORDED_AT, ph.getRecordedAt())
                    .set(PRICE, ph.getPrice())
                    .set(SOURCE, ph.getSource())
                    .where(ID.eq(ph.getId()))
                    .execute();
            return ph;
        }
    }

    public boolean deleteById(Long id) {
        int deleted = dsl.deleteFrom(PRICE_HISTORY)
                .where(ID.eq(id))
                .execute();
        return deleted > 0;
    }

    private PriceHistory mapRecordToPriceHistory(Record r) {
        if (r == null) return null;
        PriceHistory ph = new PriceHistory();
        ph.setId(r.get(ID));
        Asset a = new Asset();
        a.setId(r.get(ASSET_ID));
        ph.setAsset(a);
        ph.setRecordedAt(r.get(RECORDED_AT));
        ph.setPrice(r.get(PRICE));
        ph.setSource(r.get(SOURCE));
        return ph;
    }
}