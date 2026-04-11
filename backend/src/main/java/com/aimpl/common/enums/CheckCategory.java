package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum CheckCategory {
    DATA("数据"),
    CONFIG("配置"),
    FUNCTION("功能"),
    TRAINING("培训"),
    PERSONNEL("人员");

    private final String label;

    CheckCategory(String label) {
        this.label = label;
    }
}
