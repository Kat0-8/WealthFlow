package org.example.wealthflow.favourite.repositories;

import lombok.RequiredArgsConstructor;
import org.example.wealthflow.asset.models.Asset;
import org.example.wealthflow.common.dtos.PagedResultDto;
import org.example.wealthflow.favourite.models.Favourite;
import org.example.wealthflow.user.models.User;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertResultStep;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import org.jooq.Record;

@Repository
@RequiredArgsConstructor
public class FavouriteRepository {

    private final DSLContext dslContext;

    private final Table<?> FAV = table("favourites");
    private final Field<Long> ID = field("id", Long.class);
    private final Field<Long> USER_ID = field("user_id", Long.class);
    private final Field<Long> ASSET_ID = field("asset_id", Long.class);
    private final Field<Instant> CREATED_AT = field("created_at", Instant.class);

    public Optional<Favourite> findById(Long id) {
        return dslContext.selectFrom(FAV)
                .where(ID.eq(id))
                .fetchOptional(this::mapRecordToFavourite);
    }

    public Optional<Favourite> findByUserAndAsset(Long userId, Long assetId) {
        return dslContext.selectFrom(FAV)
                .where(USER_ID.eq(userId).and(ASSET_ID.eq(assetId)))
                .fetchOptional(this::mapRecordToFavourite);
    }

    public boolean existsByUserAndAsset(Long userId, Long assetId) {
        return dslContext.fetchExists(dslContext.selectOne().from(FAV).where(USER_ID.eq(userId).and(ASSET_ID.eq(assetId))));
    }

    public Favourite createIfNotExists(Long userId, Long assetId) {
        if (userId == null || assetId == null) throw new IllegalArgumentException("userId and assetId required");

        try {
            var insert = dslContext.insertInto(FAV)
                    .set(USER_ID, userId)
                    .set(ASSET_ID, assetId)
                    .set(CREATED_AT, Instant.now());

            InsertResultStep<?> step = insert.onConflict(USER_ID, ASSET_ID).doNothing().returning(ID, CREATED_AT);
            var rec = step.fetchOne();
            if (rec != null && rec.get(ID) != null) {
                Favourite fav = new Favourite();
                fav.setId(rec.get(ID));
                User favUser = new User();
                favUser.setId(rec.get(USER_ID));
                fav.setUser(favUser);
                Asset favAsset = new Asset();
                favAsset.setId(rec.get(ASSET_ID));
                fav.setAsset(favAsset);
                fav.setCreatedAt(rec.get(CREATED_AT));
                return fav;
            } else {
                return findByUserAndAsset(userId, assetId).orElseThrow(() ->
                        new DataAccessException("Failed to insert or find favourite for user " + userId + " asset " + assetId) {});
            }
        } catch (DataAccessException ex) {
            return findByUserAndAsset(userId, assetId)
                    .orElseThrow(() -> ex);
        }
    }

    public boolean deleteByUserAndAsset(Long userId, Long assetId) {
        int deleted = dslContext.deleteFrom(FAV).where(USER_ID.eq(userId).and(ASSET_ID.eq(assetId))).execute();
        return deleted > 0;
    }

    public PagedResultDto<Favourite> findByUserIdWithTotal(Long userId, int limit, int offset) {
        if (userId == null) throw new IllegalArgumentException("userId required");

        Condition cond = USER_ID.eq(userId);

        Field<Integer> totalField = DSL.count().over().as("total_count");

        Result<Record> result = dslContext.select(FAV.fields())
                .select(totalField)
                .from(FAV)
                .where(cond)
                .orderBy(CREATED_AT.desc())
                .limit(limit)
                .offset(offset)
                .fetch();

        List<Favourite> items = new ArrayList<>();
        long total = 0;
        for (Record r : result) {
            items.add(mapRecordToFavourite(r));
            if (total == 0 && r.field("total_count") != null) {
                Number n = r.get("total_count", Number.class);
                if (n != null) total = n.longValue();
            }
        }

        int page = offset / Math.max(1, limit);
        return PagedResultDto.<Favourite>builder()
                .items(items)
                .total(total)
                .page(page)
                .size(limit)
                .build();
    }

    private Favourite mapRecordToFavourite(org.jooq.Record r) {
        if (r == null) return null;
        Favourite f = new Favourite();
        f.setId(r.get(ID));
        Long userId = r.get(USER_ID);
        Long assetId = r.get(ASSET_ID);
        User u = new User();
        u.setId(userId);
        Asset a = new Asset();
        a.setId(assetId);
        f.setUser(u);
        f.setAsset(a);
        f.setCreatedAt(r.get(CREATED_AT));
        return f;
    }
}