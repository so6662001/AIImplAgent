package com.aimpl.domain.archive.codegen;

import com.aimpl.common.enums.ProductCategoryGroup;
import com.aimpl.common.exception.BizException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 货品编码智能生成引擎。
 * <p>
 * 编码 = 品类编码 + 规格编码串。
 * 钢材类编码按"由小到大、由薄到厚"数值编排，天然有序；
 * 非钢材类使用品类编码 + 4位顺序号。
 */
public final class ProductCodeGenerator {

    private ProductCodeGenerator() {}

    /**
     * 根据品类和解析后的规格参数生成编码。
     *
     * @param categoryCode 品类编码（来自 B3）
     * @param group        品类分组
     * @param params       解析后的规格参数
     * @param seq          顺序号（仅非钢材类使用）
     * @return 完整的货品编码
     */
    public static String generate(String categoryCode, ProductCategoryGroup group,
                                  SpecParams params, int seq) {
        if (categoryCode == null || categoryCode.isBlank()) {
            throw new BizException("品类编码不能为空，请先建立货品类别(B3)");
        }
        String specCode = switch (group) {
            case PLATE -> generatePlate(params);
            case SQUARE_PIPE -> generateSquarePipe(params);
            case ROUND_PIPE -> generateRoundPipe(params);
            case SECTION_H -> generateSectionH(params);
            case SECTION_CHANNEL -> generateChannel(params);
            case SECTION_ANGLE -> generateAngle(params);
            case WIRE_BAR -> generateWireBar(params);
            case NON_STEEL -> generateSeq(seq);
        };
        return categoryCode + specCode;
    }

    // ─── 板材：厚度码(4位) + 宽度码(4位) ───

    private static String generatePlate(SpecParams p) {
        requireNonNull(p.getThickness(), "板材厚度");
        requireNonNull(p.getWidth(), "板材宽度");
        String thk = encodeDecimal(p.getThickness(), 4, 2, "板材厚度");
        String wid = padInt(p.getWidth(), 4, "板材宽度");
        return thk + wid;
    }

    // ─── 方矩管：边长码(3位) + 边宽码(3位) + 壁厚码(3位) ───

    private static String generateSquarePipe(SpecParams p) {
        requireNonNull(p.getSideA(), "边长");
        requireNonNull(p.getSideB(), "边宽");
        requireNonNull(p.getWallThickness(), "壁厚");
        String a = padInt(p.getSideA(), 3, "边长");
        String b = padInt(p.getSideB(), 3, "边宽");
        String wt = encodeDecimal(p.getWallThickness(), 3, 1, "壁厚");
        return a + b + wt;
    }

    // ─── 圆管：外径码(4位) + 壁厚码(3位) ───

    private static String generateRoundPipe(SpecParams p) {
        requireNonNull(p.getOuterDiameter(), "外径");
        requireNonNull(p.getWallThickness(), "壁厚");
        String od = encodeDecimal(p.getOuterDiameter(), 4, 1, "外径");
        String wt = encodeDecimal(p.getWallThickness(), 3, 1, "壁厚");
        return od + wt;
    }

    // ─── H型钢：高度码(3位) + 翼宽码(3位) ───

    private static String generateSectionH(SpecParams p) {
        requireNonNull(p.getHeight(), "H型钢高度");
        requireNonNull(p.getWidth(), "H型钢翼宽");
        return padInt(p.getHeight(), 3, "高度") + padInt(p.getWidth(), 3, "翼宽");
    }

    // ─── 槽钢：号数(3位) ───

    private static String generateChannel(SpecParams p) {
        requireNonNull(p.getChannelNumber(), "槽钢号数");
        return padInt(p.getChannelNumber(), 3, "号数");
    }

    // ─── 角钢：边长码(3位) + 边厚码(3位) ───

    private static String generateAngle(SpecParams p) {
        requireNonNull(p.getSideA(), "角钢边长");
        requireNonNull(p.getThickness(), "角钢边厚");
        return padInt(p.getSideA(), 3, "边长") + encodeDecimal(p.getThickness(), 3, 1, "边厚");
    }

    // ─── 线材/建材：直径码(3位) ───

    private static String generateWireBar(SpecParams p) {
        requireNonNull(p.getDiameter(), "直径");
        return encodeDecimal(p.getDiameter(), 3, 1, "直径");
    }

    // ─── 非钢材：4位顺序号 ───

    private static String generateSeq(int seq) {
        return String.format("%04d", seq);
    }

    // ─── 工具方法 ───

    /**
     * 将带小数的数值去掉小数点后补齐到指定位数。
     * 例：3.0 (scale=1位小数) → "030"（3位）；0.5 → "005"（4位,scale=2）
     */
    private static String encodeDecimal(BigDecimal v, int totalDigits, int decimalPlaces,
                                        String fieldName) {
        BigDecimal scaled = v.setScale(decimalPlaces, RoundingMode.HALF_UP);
        long raw = scaled.movePointRight(decimalPlaces).longValueExact();
        String s = String.valueOf(raw);
        if (s.length() > totalDigits) {
            throw new BizException(fieldName + " 值 " + v + " 编码后超过 " + totalDigits + " 位，无法生成编码");
        }
        return String.format("%" + totalDigits + "s", s).replace(' ', '0');
    }

    /**
     * 将整数部分补齐到指定位数。
     */
    private static String padInt(BigDecimal v, int digits, String fieldName) {
        long intVal = v.setScale(0, RoundingMode.HALF_UP).longValueExact();
        String s = String.valueOf(intVal);
        if (s.length() > digits) {
            throw new BizException(fieldName + " 值 " + v + " 编码后超过 " + digits + " 位");
        }
        return String.format("%" + digits + "s", s).replace(' ', '0');
    }

    private static void requireNonNull(Object v, String fieldName) {
        if (v == null) {
            throw new BizException("生成编码所需的 " + fieldName + " 参数缺失，请检查规格格式");
        }
    }
}
