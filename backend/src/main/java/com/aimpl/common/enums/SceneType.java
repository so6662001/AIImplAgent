package com.aimpl.common.enums;

import lombok.Getter;

@Getter
public enum SceneType {
    PURCHASE_INBOUND("采购入库"),
    SALES_OUTBOUND("销售出库"),
    INVENTORY_CHECK("盘点"),
    TRANSFER("调拨"),
    FINANCIAL_SETTLEMENT("财务结算"),
    FULL_PROCESS("全流程");

    private final String label;

    SceneType(String label) {
        this.label = label;
    }
}
