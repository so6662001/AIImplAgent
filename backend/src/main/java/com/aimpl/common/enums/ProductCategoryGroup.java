package com.aimpl.common.enums;

import lombok.Getter;

/**
 * 货品大类分组，决定编码规则。
 */
@Getter
public enum ProductCategoryGroup {
    PLATE("板材类"),
    SQUARE_PIPE("方矩管"),
    ROUND_PIPE("圆管类"),
    SECTION_H("H型钢"),
    SECTION_CHANNEL("槽钢"),
    SECTION_ANGLE("角钢"),
    WIRE_BAR("线材/建材"),
    NON_STEEL("非钢材");

    private final String label;

    ProductCategoryGroup(String label) {
        this.label = label;
    }
}
