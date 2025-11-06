package org.example.wealthflow.dtos.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRoleDto {
    USER, ADMIN;

    @JsonCreator
    public static UserRoleDto fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return UserRoleDto.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Role value must be one of " + java.util.Arrays.toString(UserRoleDto.values()));
        }
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
