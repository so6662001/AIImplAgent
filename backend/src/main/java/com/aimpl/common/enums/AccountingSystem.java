package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum AccountingSystem {
    ENTERPRISE("企业会计准则"),
    SMALL_ENTERPRISE("小企业会计准则");

    private final String label;

    AccountingSystem(String label) {
        this.label = label;
    }
}
