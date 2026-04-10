package com.aimpl.domain.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_project_plan")
public class ProjectPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String planName;

    private Integer totalDays;

    private String milestones;

    private String wbsItems;

    private String resources;

    private String risks;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
