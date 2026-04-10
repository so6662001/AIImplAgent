package com.aimpl.domain.workforce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_engineer_worklog")
public class EngineerWorklog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long engineerId;

    private Long projectId;

    private LocalDate workDate;

    private String tasksPlan;

    private String tasksCompleted;

    private String documentsSubmitted;

    private String issues;

    private String nextDayPlan;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
