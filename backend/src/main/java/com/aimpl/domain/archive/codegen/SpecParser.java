package com.aimpl.domain.archive.codegen;

import com.aimpl.common.enums.ProductCategoryGroup;
import com.aimpl.common.exception.BizException;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将规格字符串解析为结构化的 SpecParams。
 * 按品类分组选择不同的解析策略。
 */
public final class SpecParser {

    private static final String SEP = "[*×xX]";

    private SpecParser() {}

    public static SpecParams parse(ProductCategoryGroup group, String spec) {
        if (spec == null || spec.isBlank()) {
            throw new BizException("规格不能为空");
        }
        String s = spec.trim()
                .replaceAll("[Φφ]", "")
                .replaceAll("mm", "")
                .replaceAll("□", "");

        return switch (group) {
            case PLATE -> parsePlate(s);
            case SQUARE_PIPE -> parseSquarePipe(s);
            case ROUND_PIPE -> parseRoundPipe(s);
            case SECTION_H -> parseSectionH(s);
            case SECTION_CHANNEL -> parseChannel(s);
            case SECTION_ANGLE -> parseAngle(s);
            case WIRE_BAR -> parseWireBar(s);
            case NON_STEEL -> SpecParams.builder().build();
        };
    }

    /** 板材: 厚*宽*长(或C) */
    private static SpecParams parsePlate(String s) {
        String[] parts = s.split(SEP);
        if (parts.length < 2) {
            throw new BizException("板材规格格式错误，期望: 厚*宽*长(或C)，实际: " + s);
        }
        boolean coil = parts.length >= 3
                && (parts[2].equalsIgnoreCase("C") || parts[2].contains("卷"));

        return SpecParams.builder()
                .thickness(parseDec(parts[0], "厚度"))
                .width(parseDec(parts[1], "宽度"))
                .length(coil ? null : (parts.length >= 3 ? parseDec(parts[2], "长度") : null))
                .coil(coil)
                .build();
    }

    /** 方矩管: 边长*边宽*壁厚 */
    private static SpecParams parseSquarePipe(String s) {
        String[] parts = s.split(SEP);
        if (parts.length < 3) {
            throw new BizException("方矩管规格格式错误，期望: 边长*边宽*壁厚，实际: " + s);
        }
        return SpecParams.builder()
                .sideA(parseDec(parts[0], "边长"))
                .sideB(parseDec(parts[1], "边宽"))
                .wallThickness(parseDec(parts[2], "壁厚"))
                .build();
    }

    /** 圆管: 外径*壁厚 */
    private static SpecParams parseRoundPipe(String s) {
        String[] parts = s.split(SEP);
        if (parts.length < 2) {
            throw new BizException("圆管规格格式错误，期望: 外径*壁厚，实际: " + s);
        }
        return SpecParams.builder()
                .outerDiameter(parseDec(parts[0], "外径"))
                .wallThickness(parseDec(parts[1], "壁厚"))
                .build();
    }

    /** H型钢: H*B*tw*tf 或 H*B */
    private static SpecParams parseSectionH(String s) {
        String cleaned = s.replaceAll("(?i)^[hH]", "");
        String[] parts = cleaned.split(SEP);
        if (parts.length < 2) {
            throw new BizException("H型钢规格格式错误，期望: H*B，实际: " + s);
        }
        return SpecParams.builder()
                .height(parseDec(parts[0], "高度"))
                .width(parseDec(parts[1], "翼宽"))
                .build();
    }

    /** 槽钢: 号数(如 5# / 10 / 20a) */
    private static SpecParams parseChannel(String s) {
        Matcher m = Pattern.compile("(\\d+\\.?\\d*)").matcher(s);
        if (!m.find()) {
            throw new BizException("槽钢规格格式错误: " + s);
        }
        return SpecParams.builder()
                .channelNumber(new BigDecimal(m.group(1)))
                .build();
    }

    /** 角钢: 边长*边厚 */
    private static SpecParams parseAngle(String s) {
        String[] parts = s.split(SEP);
        if (parts.length < 2) {
            throw new BizException("角钢规格格式错误，期望: 边长*边厚，实际: " + s);
        }
        return SpecParams.builder()
                .sideA(parseDec(parts[0], "边长"))
                .thickness(parseDec(parts[1], "边厚"))
                .build();
    }

    /** 线材/建材: 直径 */
    private static SpecParams parseWireBar(String s) {
        Matcher m = Pattern.compile("(\\d+\\.?\\d*)").matcher(s);
        if (!m.find()) {
            throw new BizException("线材规格格式错误: " + s);
        }
        return SpecParams.builder()
                .diameter(new BigDecimal(m.group(1)))
                .build();
    }

    private static BigDecimal parseDec(String v, String fieldName) {
        try {
            return new BigDecimal(v.trim());
        } catch (NumberFormatException e) {
            throw new BizException(fieldName + " 数值格式错误: " + v);
        }
    }
}
