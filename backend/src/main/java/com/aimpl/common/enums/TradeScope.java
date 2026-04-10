package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum TradeScope {
    DOMESTIC("内贸"),
    FOREIGN("外贸"),
    BOTH("内外贸");

    private final String label;

    TradeScope(String label) {
        this.label = label;
    }
}
