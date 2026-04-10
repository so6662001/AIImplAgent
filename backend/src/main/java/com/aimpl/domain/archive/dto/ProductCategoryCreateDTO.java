package com.aimpl.domain.archive.dto;

import com.aimpl.common.enums.ProductCategoryGroup;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductCategoryCreateDTO {

    @NotBlank(message = "品类编码不能为空")
    @Size(max = 20, message = "品类编码长度不能超过20位")
    private String categoryCode;

    @NotBlank(message = "品类名称不能为空")
    @Size(max = 60, message = "品类名称长度不能超过60")
    private String categoryName;

    private Long parentId;

    @Min(value = 1, message = "品类层级最小为1")
    private Integer level;

    @Min(value = 0, message = "排序值不能为负数")
    @Max(value = 999999, message = "排序值过大")
    private Integer sortOrder;

    private ProductCategoryGroup categoryGroup;

    @Size(max = 10, message = "默认单位长度不能超过10")
    private String defaultUnit;
}
