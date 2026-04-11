package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum EngineerLevel {
    INTERN("实习"),
    JUNIOR("初级实施"),
    MID("中级实施"),
    SENIOR("高级实施"),
    PM("PM"),
    SENIOR_PM("高级PM"),
    EXPERT_PM("资深PM");

    private final String label;

    EngineerLevel(String label) {
        this.label = label;
    }
}
