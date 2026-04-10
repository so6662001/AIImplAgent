package com.aimpl.domain.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AgentConfigCreateDTO {

    @NotBlank(message = "智能体编码不能为空")
    @Size(max = 30, message = "智能体编码长度不能超过30")
    private String agentCode;

    @NotBlank(message = "智能体名称不能为空")
    @Size(max = 60, message = "智能体名称长度不能超过60")
    private String agentName;

    private String description;

    @NotNull(message = "LLM供应商ID不能为空")
    private Long llmProviderId;

    private Long fallbackLlmProviderId;

    @NotBlank(message = "提示词模板不能为空")
    private String promptTemplate;

    private Boolean ragEnabled = false;

    private String ragCollectionName;
}
