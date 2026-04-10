package com.aimpl.domain.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_account_subject")
public class AccountSubject {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String subjectCode;

    private String subjectName;

    private String parentCode;

    private String subjectCategory;

    private String balanceDirection;

    private String auxiliaryAccounting;

    private Boolean isLeaf;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
