package com.aimpl.domain.openingbalance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_subject_balance")
public class SubjectBalance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String subjectCode;

    private String subjectName;

    private BigDecimal debitBalance;

    private BigDecimal creditBalance;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
