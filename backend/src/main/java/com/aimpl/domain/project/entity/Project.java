package com.aimpl.domain.project.entity;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.common.enums.ProjectStatus;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_project")
public class Project {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String projectCode;

    private String customerName;

    private IndustryType industryType;

    private String scale;

    private Long pmId;

    private ProjectStatus status;

    private String modules;

    private String region;

    private LocalDate startDate;

    private LocalDate endDate;

    private String specialRequirements;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
