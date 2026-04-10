package com.aimpl.domain.openingbalance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_other_receivable")
public class OtherReceivable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String subjectCode;

    private String objectType;

    private Long objectId;

    private String objectName;

    private String summary;

    private BigDecimal amount;

    private LocalDate occurDate;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
