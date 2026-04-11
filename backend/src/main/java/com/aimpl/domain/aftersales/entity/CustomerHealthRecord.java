package com.aimpl.domain.aftersales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_customer_health")
public class CustomerHealthRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String customerName;

    private LocalDate checkDate;

    private Integer ticketCount30d;

    private String ticketTrend;

    private Integer openTicketCount;

    private BigDecimal avgResolutionHours;

    private BigDecimal customerSatisfactionAvg;

    private Integer healthScore;

    private String healthLevel;

    private String careActions;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
