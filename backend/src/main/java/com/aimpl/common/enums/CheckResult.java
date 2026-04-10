package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum CheckResult {
    PASS("通过"),
    FAIL("不通过"),
    WARNING("警告"),
    UNCHECKED("未检查");

    private final String label;

    CheckResult(String label) {
        this.label = label;
    }
}
