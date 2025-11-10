package org.example.wealthflow.notificationrule.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationRuleDirectionDto {
    ABOVE, BELOW;

    @JsonCreator
    public static NotificationRuleDirectionDto fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return NotificationRuleDirectionDto.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Direction value must be one of "
                    + java.util.Arrays.toString(NotificationRuleDirectionDto.values()));
        }
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
