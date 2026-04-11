package com.aimpl.domain.openingbalance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_account_balance")
public class AccountBalance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long bankAccountId;

    private String currency;

    private BigDecimal openingBalance;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
