package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum SimulationStatus {
    PENDING("待执行"),
    RUNNING("执行中"),
    PASSED("通过"),
    FAILED("失败");

    private final String label;

    SimulationStatus(String label) {
        this.label = label;
    }
}
