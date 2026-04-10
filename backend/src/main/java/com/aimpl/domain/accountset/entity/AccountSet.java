package com.aimpl.domain.accountset.entity;

import com.aimpl.common.enums.AccountSetStatus;
import com.aimpl.common.enums.AccountingSystem;
import com.aimpl.common.enums.PricingMethod;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_account_set")
public class AccountSet {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String setName;

    private AccountingSystem accountingSystem;

    private PricingMethod pricingMethod;

    private Integer qtyDecimals;

    private Integer wgtDecimals;

    private Integer prcDecimals;

    private Integer amtDecimals;

    private Boolean useWeight;

    private String currency;

    private Integer fiscalYearStart;

    private AccountSetStatus status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
