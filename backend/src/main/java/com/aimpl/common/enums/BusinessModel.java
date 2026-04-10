package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum BusinessModel {
    STEEL_TRADER("钢贸商"),
    STEEL_MILL("钢厂"),
    PROCESSING_CENTER("加工中心"),
    INTEGRATED_SERVICE("综合服务商");

    private final String label;

    BusinessModel(String label) {
        this.label = label;
    }
}
