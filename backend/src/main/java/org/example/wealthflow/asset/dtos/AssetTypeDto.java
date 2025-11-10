package org.example.wealthflow.asset.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AssetTypeDto {
    STOCK, CRYPTO;

    @JsonCreator
    public static AssetTypeDto fromString(String value) {
        if(value == null) {
            return null;
        }
        try {
            return AssetTypeDto.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Type value must be one of "
                    + java.util.Arrays.toString(AssetTypeDto.values()));
        }
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
