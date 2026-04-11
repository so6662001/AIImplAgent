package com.aimpl.domain.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_agent_decision_log")
public class AgentDecisionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String agentCode;

    private Long projectId;

    private Long userId;

    private String triggerType;

    private String inputSummary;

    private String outputSummary;

    private String modelUsed;

    private String knowledgeUsed;

    private BigDecimal confidence;

    private Integer executionTimeMs;

    private Boolean humanReviewed;

    private String reviewResult;

    private String reviewComment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
