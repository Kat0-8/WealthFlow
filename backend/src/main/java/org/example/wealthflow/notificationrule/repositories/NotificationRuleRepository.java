package org.example.wealthflow.notificationrule.repositories;

import lombok.RequiredArgsConstructor;
import org.example.wealthflow.asset.models.Asset;
import org.example.wealthflow.common.dtos.PagedResultDto;
import org.example.wealthflow.notificationrule.models.NotificationRule;
import org.example.wealthflow.user.models.User;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.Record;
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
public class NotificationRuleRepository {

    private final DSLContext dslContext;

    private final Table<?> NOTIFICATION_RULES = table("notification_rules");
    private final Field<Long> ID = field("id", Long.class);
    private final Field<Long> USER_ID = field("user_id", Long.class);
    private final Field<Long> ASSET_ID = field("asset_id", Long.class);
    private final Field<BigDecimal> TARGET_PRICE = field("target_price", BigDecimal.class);
    private final Field<String> DIRECTION = field("direction", String.class);
    private final Field<Boolean> ENABLED = field("enabled", Boolean.class);
    private final Field<Boolean> REPEAT = field("repeat_notification", Boolean.class);
    private final Field<Instant> LAST_TRIGGERED = field("last_triggered", Instant.class);
    private final Field<Instant> CREATED_AT = field("created_at", Instant.class);

    public PagedResultDto<NotificationRule> findByUserIdOrderByCreatedAtDesc(Long userId, int limit, int offset) {
        Condition cond = USER_ID.eq(userId);

        var totalField = DSL.count().over().as("total_count");

        Result<Record> result = dslContext.select(NOTIFICATION_RULES.fields())
                .select(totalField)
                .from(NOTIFICATION_RULES)
                .where(cond)
                .orderBy(CREATED_AT.desc())
                .limit(limit)
                .offset(offset)
                .fetch();

        List<NotificationRule> items = new ArrayList<>();
        long total = 0;
        for (Record r : result) {
            items.add(mapRecordToNotificationRule(r));
            if (total == 0 && r.field("total_count") != null) {
                Number n = r.get("total_count", Number.class);
                if (n != null) total = n.longValue();
            }
        }

        return PagedResultDto.<NotificationRule>builder()
                .items(items)
                .total(total)
                .page(offset / Math.max(1, limit))
                .size(limit)
                .build();
    }

    public Optional<NotificationRule> findById(Long id) {
        return dslContext.selectFrom(NOTIFICATION_RULES)
                .where(ID.eq(id))
                .fetchOptional(this::mapRecordToNotificationRule);
    }

    public List<NotificationRule> findByAssetIdEnabled(Long assetId) {
        Condition cond = ASSET_ID.eq(assetId).and(ENABLED.eq(true));

        Result<Record> result = dslContext.select(NOTIFICATION_RULES.fields())
                .from(NOTIFICATION_RULES)
                .where(cond)
                .orderBy(CREATED_AT.desc())
                .fetch();

        List<NotificationRule> items = new ArrayList<>(result.size());
        for (Record r : result) {
            items.add(mapRecordToNotificationRule(r));
        }

        return items;
    }

    public NotificationRule save(NotificationRule nr) {
        if (nr.getId() == null) {
            Long newId = dslContext.insertInto(NOTIFICATION_RULES)
                    .set(USER_ID, nr.getUser().getId())
                    .set(ASSET_ID, nr.getAsset().getId())
                    .set(TARGET_PRICE, nr.getTargetPrice())
                    .set(DIRECTION, nr.getDirection() == null ? null : nr.getDirection().name())
                    .set(ENABLED, nr.isEnabled())
                    .set(REPEAT, nr.isRepeat())
                    .set(LAST_TRIGGERED, nr.getLastTriggered())
                    .set(CREATED_AT, nr.getCreatedAt() == null ? Instant.now() : nr.getCreatedAt())
                    .returning(ID)
                    .fetchOne(ID);
            nr.setId(newId);
            return nr;
        } else {
            dslContext.update(NOTIFICATION_RULES)
                    .set(TARGET_PRICE, nr.getTargetPrice())
                    .set(DIRECTION, nr.getDirection() == null ? null : nr.getDirection().name())
                    .set(ENABLED, nr.isEnabled())
                    .set(REPEAT, nr.isRepeat())
                    .set(LAST_TRIGGERED, nr.getLastTriggered())
                    .where(ID.eq(nr.getId()))
                    .execute();
            return nr;
        }
    }

    public boolean deleteById(Long id) {
        int deleted = dslContext.deleteFrom(NOTIFICATION_RULES)
                .where(ID.eq(id))
                .execute();
        return deleted > 0;
    }

    private NotificationRule mapRecordToNotificationRule(Record r) {
        if (r == null) return null;
        NotificationRule nr = new NotificationRule();
        nr.setId(r.get(ID));

        User u = new org.example.wealthflow.user.models.User();
        u.setId(r.get(USER_ID));
        nr.setUser(u);

        Asset a = new org.example.wealthflow.asset.models.Asset();
        a.setId(r.get(ASSET_ID));
        nr.setAsset(a);

        nr.setTargetPrice(r.get(TARGET_PRICE));

        String dir = r.get(DIRECTION);
        if (dir != null) {
            try {
                nr.setDirection(NotificationRule.Direction.valueOf(dir));
            } catch (IllegalArgumentException ex) {
                nr.setDirection(null);
            }
        } else {
            nr.setDirection(null);
        }

        Boolean enabled = r.get(ENABLED);
        nr.setEnabled(enabled == null || enabled);

        Boolean repeat = r.get(REPEAT);
        nr.setRepeat(repeat != null && repeat);

        nr.setLastTriggered(r.get(LAST_TRIGGERED));
        nr.setCreatedAt(r.get(CREATED_AT));
        return nr;
    }
}
