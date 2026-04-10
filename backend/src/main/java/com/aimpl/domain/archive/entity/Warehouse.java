package com.aimpl.domain.archive.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_warehouse")
public class Warehouse {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String warehouseCode;

    private String warehouseName;

    private String warehouseType;

    private String warehouseNature;

    private String managementMode;

    private String address;

    private String contact;

    private String phone;

    private BigDecimal areaSqm;

    private Integer craneCount;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
