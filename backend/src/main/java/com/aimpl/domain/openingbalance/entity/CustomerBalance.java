package com.aimpl.domain.openingbalance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_customer_balance")
public class CustomerBalance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long customerId;

    private String docType;

    private String docNo;

    private LocalDate docDate;

    private BigDecimal receivableAmount;

    private BigDecimal receivedAmount;

    private BigDecimal balance;

    private String balanceType;

    private LocalDate expectedDate;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
