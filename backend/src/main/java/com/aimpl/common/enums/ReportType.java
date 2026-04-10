package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum ReportType {
    MILESTONE("里程碑报告"),
    FINAL("终验报告");

    private final String label;

    ReportType(String label) {
        this.label = label;
    }
}
