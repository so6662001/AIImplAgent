package com.aimpl.domain.archive.codegen;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 从规格字符串中解析出的数值参数。
 * 不同品类使用不同字段组合。
 */
@Data
@Builder
public class SpecParams {

    /** 板材厚度 / 角钢边厚 (mm) */
    private BigDecimal thickness;

    /** 板材宽度 / 型材翼宽 (mm) */
    private BigDecimal width;

    /** 板材长度 (mm)，卷板为 null */
    private BigDecimal length;

    /** 是否卷板 */
    private boolean coil;

    /** 方矩管边长 / 角钢边长 (mm) */
    private BigDecimal sideA;

    /** 方矩管边宽 (mm) */
    private BigDecimal sideB;

    /** 管材壁厚 (mm) */
    private BigDecimal wallThickness;

    /** 圆管外径 (mm) */
    private BigDecimal outerDiameter;

    /** 线材/建材直径 (mm) */
    private BigDecimal diameter;

    /** 型材高度 (mm) */
    private BigDecimal height;

    /** 槽钢号数 */
    private BigDecimal channelNumber;
}
