package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum ServerStatus {
    CONNECTED("已连接"),
    DISCONNECTED("断开"),
    UNCONFIGURED("未配置");

    private final String label;

    ServerStatus(String label) {
        this.label = label;
    }
}
