package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum TradeMode {
    DIRECT("直营"),
    AGENT("代理"),
    MIXED("混合");

    private final String label;

    TradeMode(String label) {
        this.label = label;
    }
}
