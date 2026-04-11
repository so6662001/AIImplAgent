package com.aimpl.domain.research.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_research_report")
public class ResearchReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long profileId;

    private String reportContent;

    private String overallRiskLevel;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
