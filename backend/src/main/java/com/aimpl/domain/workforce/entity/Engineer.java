package com.aimpl.domain.workforce.entity;

import com.aimpl.common.enums.EngineerLevel;
import com.aimpl.common.enums.EngineerStatus;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_engineer")
public class Engineer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String engineerCode;

    private String name;

    private EngineerLevel level;

    private String skills;

    private EngineerStatus currentStatus;

    private Long currentProjectId;

    private LocalDate joinDate;

    private String phone;

    private String email;

    private BigDecimal compositeScore;

    private BigDecimal monthlyIdleRate;

    private Integer monthlyProjectCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
