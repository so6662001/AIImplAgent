package com.aimpl.domain.agent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentDecisionLogDTO {

    @NotBlank(message = "agentCode不能为空")
    private String agentCode;

    @NotBlank(message = "inputSummary不能为空")
    private String inputSummary;

    private String outputSummary;

    private String modelUsed;

    private Long projectId;

    private Long userId;

    private String triggerType;

    @Min(value = 0, message = "executionTimeMs不能为负数")
    private Integer executionTimeMs;
}
