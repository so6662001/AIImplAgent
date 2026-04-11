package com.aimpl.archive.codegen;

import com.aimpl.common.enums.ProductCategoryGroup;
import com.aimpl.domain.archive.codegen.ProductCodeGenerator;
import com.aimpl.domain.archive.codegen.SpecParams;
import com.aimpl.domain.archive.codegen.SpecParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductCodeGeneratorTest {

    @Test
    void testPlateCode() {
        SpecParams p = SpecParser.parse(ProductCategoryGroup.PLATE, "3.0*1250*C");
        String code = ProductCodeGenerator.generate("BC", ProductCategoryGroup.PLATE, p, 0);
        assertEquals("BC03001250", code);
    }

    @Test
    void testPlateThinCode() {
        SpecParams p = SpecParser.parse(ProductCategoryGroup.PLATE, "0.5*1000*C");
        String code = ProductCodeGenerator.generate("BC", ProductCategoryGroup.PLATE, p, 0);
        assertEquals("BC00501000", code);
    }

    @Test
    void testPlateOrdering() {
        String c1 = generatePlateCode("0.5*1000*C");
        String c2 = generatePlateCode("0.5*1250*C");
        String c3 = generatePlateCode("3.0*1250*C");
        String c4 = generatePlateCode("12.0*2200*6000");

        assertTrue(c1.compareTo(c2) < 0, "0.5*1000 should be before 0.5*1250");
        assertTrue(c2.compareTo(c3) < 0, "0.5*1250 should be before 3.0*1250");
        assertTrue(c3.compareTo(c4) < 0, "3.0*1250 should be before 12.0*2200");
    }

    @Test
    void testSquarePipeCode() {
        SpecParams p = SpecParser.parse(ProductCategoryGroup.SQUARE_PIPE, "20*40*1.5");
        String code = ProductCodeGenerator.generate("GFJ", ProductCategoryGroup.SQUARE_PIPE, p, 0);
        assertEquals("GFJ020040015", code);
    }

    @Test
    void testSquarePipeOrdering() {
        String c1 = generateSquarePipeCode("20*20*1.0");
        String c2 = generateSquarePipeCode("20*20*2.0");
        String c3 = generateSquarePipeCode("20*40*1.5");
        String c4 = generateSquarePipeCode("100*100*4.0");
        String c5 = generateSquarePipeCode("200*100*6.0");

        assertTrue(c1.compareTo(c2) < 0, "20*20*1.0 < 20*20*2.0 (壁厚由薄到厚)");
        assertTrue(c2.compareTo(c3) < 0, "20*20*2.0 < 20*40*1.5 (边宽由小到大)");
        assertTrue(c3.compareTo(c4) < 0, "20*40*1.5 < 100*100*4.0 (边长由小到大)");
        assertTrue(c4.compareTo(c5) < 0, "100*100*4.0 < 200*100*6.0");
    }

    @Test
    void testRoundPipeCode() {
        SpecParams p = SpecParser.parse(ProductCategoryGroup.ROUND_PIPE, "Φ219*8");
        String code = ProductCodeGenerator.generate("GWF", ProductCategoryGroup.ROUND_PIPE, p, 0);
        assertEquals("GWF2190080", code);
    }

    @Test
    void testSectionHCode() {
        SpecParams p = SpecParser.parse(ProductCategoryGroup.SECTION_H, "H200*200*8*12");
        String code = ProductCodeGenerator.generate("XH", ProductCategoryGroup.SECTION_H, p, 0);
        assertEquals("XH200200", code);
    }

    @Test
    void testWireBarCode() {
        SpecParams p = SpecParser.parse(ProductCategoryGroup.WIRE_BAR, "Φ25");
        String code = ProductCodeGenerator.generate("JL", ProductCategoryGroup.WIRE_BAR, p, 0);
        assertEquals("JL250", code);
    }

    @Test
    void testNonSteelCode() {
        SpecParams p = SpecParser.parse(ProductCategoryGroup.NON_STEEL, "打包带");
        String code = ProductCodeGenerator.generate("QT", ProductCategoryGroup.NON_STEEL, p, 1);
        assertEquals("QT0001", code);
    }

    @Test
    void testChannelCode() {
        SpecParams p = SpecParser.parse(ProductCategoryGroup.SECTION_CHANNEL, "10#");
        String code = ProductCodeGenerator.generate("XC", ProductCategoryGroup.SECTION_CHANNEL, p, 0);
        assertEquals("XC010", code);
    }

    @Test
    void testAngleCode() {
        SpecParams p = SpecParser.parse(ProductCategoryGroup.SECTION_ANGLE, "30*3");
        String code = ProductCodeGenerator.generate("XJ", ProductCategoryGroup.SECTION_ANGLE, p, 0);
        assertEquals("XJ030030", code);
    }

    @Test
    void testSpecParserVariousFormats() {
        SpecParams p1 = SpecParser.parse(ProductCategoryGroup.PLATE, "3.0×1250×C");
        assertEquals(0, p1.getThickness().compareTo(new java.math.BigDecimal("3.0")));

        SpecParams p2 = SpecParser.parse(ProductCategoryGroup.SQUARE_PIPE, "□20×40×1.5");
        assertEquals(0, p2.getSideA().compareTo(new java.math.BigDecimal("20")));
    }

    @Test
    void testInvalidSpecThrows() {
        assertThrows(Exception.class, () ->
                SpecParser.parse(ProductCategoryGroup.PLATE, "abc"));
        assertThrows(Exception.class, () ->
                SpecParser.parse(ProductCategoryGroup.SQUARE_PIPE, "20*30"));
    }

    private String generatePlateCode(String spec) {
        SpecParams p = SpecParser.parse(ProductCategoryGroup.PLATE, spec);
        return ProductCodeGenerator.generate("BC", ProductCategoryGroup.PLATE, p, 0);
    }

    private String generateSquarePipeCode(String spec) {
        SpecParams p = SpecParser.parse(ProductCategoryGroup.SQUARE_PIPE, spec);
        return ProductCodeGenerator.generate("GFJ", ProductCategoryGroup.SQUARE_PIPE, p, 0);
    }
}
