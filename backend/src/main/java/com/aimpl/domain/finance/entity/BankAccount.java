package com.aimpl.domain.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_bank_account")
public class BankAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String accountCode;

    private String accountName;

    private String accountType;

    private String bankName;

    private String bankAccountNo;

    private String bankBranch;

    private String currency;

    private String subjectCode;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
