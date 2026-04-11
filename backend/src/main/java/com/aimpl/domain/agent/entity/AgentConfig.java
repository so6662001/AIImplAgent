package com.aimpl.domain.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_agent_config")
public class AgentConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String agentCode;

    private String agentName;

    private String description;

    private Long llmProviderId;

    private Long fallbackLlmProviderId;

    private String promptTemplate;

    private Boolean ragEnabled;

    private String ragCollectionName;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
