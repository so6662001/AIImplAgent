package com.aimpl.domain.aftersales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_after_sales_ticket")
public class AfterSalesTicket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String customerName;

    private String reporterName;

    private String reporterContact;

    private String channel;

    private String intentType;

    private String title;

    private String description;

    private String slaPriority;

    private LocalDateTime slaDeadline;

    private String status;

    private Long assignedEngineerId;

    private String assignedEngineerName;

    private String resolution;

    private LocalDateTime resolvedAt;

    private Boolean knowledgeCreated;

    private Integer customerSatisfaction;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
