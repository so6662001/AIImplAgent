package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum ProjectStatus {
    PENDING("待启动"),
    RESEARCH("调研中"),
    PLAN("计划中"),
    TRAINING("培训中"),
    DATA_IMPORT("数据导入"),
    GO_LIVE("上线中"),
    FOLLOW_UP("跟进辅助"),
    DELIVERED("已交付"),
    AFTER_SALES("售后阶段");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }
}
