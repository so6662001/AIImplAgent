package com.aimpl.domain.archive.entity;

import com.aimpl.common.enums.ProductCategoryGroup;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_product_category")
public class ProductCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String categoryCode;

    private String categoryName;

    private Long parentId;

    private Integer level;

    private Integer sortOrder;

    private ProductCategoryGroup categoryGroup;

    private String defaultUnit;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
