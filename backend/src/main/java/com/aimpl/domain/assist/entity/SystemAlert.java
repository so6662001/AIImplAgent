package com.aimpl.domain.assist.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_system_alert")
public class SystemAlert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String alertType;

    private String severity;

    private String title;

    private String description;

    private String suggestion;

    private Boolean acknowledged;

    private String acknowledgedBy;

    private LocalDateTime acknowledgedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
