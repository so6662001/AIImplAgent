package com.aimpl.domain.openingbalance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_supplier_balance")
public class SupplierBalance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long supplierId;

    private String docType;

    private String docNo;

    private LocalDate docDate;

    private BigDecimal payableAmount;

    private BigDecimal paidAmount;

    private BigDecimal balance;

    private String balanceType;

    private LocalDate expectedDate;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
