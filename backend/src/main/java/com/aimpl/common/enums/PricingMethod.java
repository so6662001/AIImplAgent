package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum PricingMethod {
    MOVING_WEIGHTED_AVG("移动加权"),
    FIFO("先进先出"),
    SPECIFIC_ID("个别计价");

    private final String label;

    PricingMethod(String label) {
        this.label = label;
    }
}
