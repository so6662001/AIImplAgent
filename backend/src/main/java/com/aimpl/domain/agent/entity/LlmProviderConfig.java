package com.aimpl.domain.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_llm_provider_config")
public class LlmProviderConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String providerName;

    private String providerType;

    private String apiEndpoint;

    private String apiKey;

    private String modelName;

    private Integer maxTokens;

    private BigDecimal temperature;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
