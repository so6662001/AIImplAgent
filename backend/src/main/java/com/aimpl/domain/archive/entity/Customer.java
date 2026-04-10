package com.aimpl.domain.archive.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_customer")
public class Customer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String customerCode;

    private String fullName;

    private String shortName;

    private String customerType;

    private String creditCode;

    private String contact;

    private String phone;

    private String province;

    private String city;

    private String district;

    private String address;

    private String industry;

    private String category;

    private String settlementMethod;

    private BigDecimal creditLimit;

    private BigDecimal taxRate;

    private String invoiceTitle;

    private String invoiceTaxNo;

    private Long salesRepId;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
