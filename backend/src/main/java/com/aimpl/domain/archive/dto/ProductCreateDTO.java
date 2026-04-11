package com.aimpl.domain.archive.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateDTO {

    /** 可选：为空时由系统自动生成 */
    @Size(max = 40, message = "货品编码长度不能超过40位")
    private String productCode;

    @NotBlank(message = "品名不能为空")
    @Size(max = 60, message = "品名长度不能超过60")
    private String productName;

    @NotBlank(message = "规格不能为空")
    @Size(max = 80, message = "规格长度不能超过80")
    private String spec;

    @Size(max = 30, message = "材质长度不能超过30")
    private String material;

    @Size(max = 60, message = "钢厂名称长度不能超过60")
    private String steelMill;

    @Size(max = 30, message = "表面/镀层长度不能超过30")
    private String surface;

    @NotNull(message = "货品类别不能为空")
    private Long categoryId;

    @Size(max = 10, message = "计量单位长度不能超过10")
    private String unit;

    @Size(max = 10, message = "计价单位长度不能超过10")
    private String pricingUnit;

    @DecimalMin(value = "0", message = "税率不能为负数")
    @DecimalMax(value = "1", message = "税率不能大于1")
    private BigDecimal taxRate;
}
