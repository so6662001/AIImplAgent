package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum ReportStatus {
    DRAFT("草稿"),
    SUBMITTED("已提交"),
    CONFIRMED("已确认");

    private final String label;

    ReportStatus(String label) {
        this.label = label;
    }
}
