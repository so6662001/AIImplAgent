package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum EngineerStatus {
    IDLE("空闲"),
    ON_PROJECT("项目中"),
    TRAINING("培训中"),
    LEAVE("休假");

    private final String label;

    EngineerStatus(String label) {
        this.label = label;
    }
}
