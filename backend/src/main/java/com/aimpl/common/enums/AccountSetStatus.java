package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum AccountSetStatus {
    DRAFT("草稿"),
    ACTIVE("启用"),
    LOCKED("锁定");

    private final String label;

    AccountSetStatus(String label) {
        this.label = label;
    }
}
