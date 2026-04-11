package com.aimpl.domain.assist.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_support_ticket")
public class SupportTicket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long reporterId;

    private String reporterName;

    private String title;

    private String description;

    private String level;

    private String category;

    private String status;

    private String assignedTo;

    private String resolution;

    private LocalDateTime resolvedAt;

    private String aiSuggestion;

    private String priority;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
