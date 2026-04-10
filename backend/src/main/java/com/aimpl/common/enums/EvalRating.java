package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum EvalRating {
    EXCELLENT("卓越"),
    GOOD("优良"),
    QUALIFIED("合格"),
    NEEDS_IMPROVEMENT("待改进"),
    UNQUALIFIED("不合格");

    private final String label;

    EvalRating(String label) {
        this.label = label;
    }
}
