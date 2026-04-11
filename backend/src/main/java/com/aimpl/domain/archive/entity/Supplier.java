package com.aimpl.domain.archive.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_supplier")
public class Supplier {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String supplierCode;

    private String fullName;

    private String shortName;

    private String supplierType;

    private String creditCode;

    private String contact;

    private String phone;

    private String address;

    private String settlementMethod;

    private BigDecimal taxRate;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
