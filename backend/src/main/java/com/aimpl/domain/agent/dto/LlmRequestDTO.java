package com.aimpl.domain.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LlmRequestDTO {

    @NotBlank(message = "智能体编码不能为空")
    private String agentCode;

    @NotBlank(message = "提示词不能为空")
    @Size(max = 10000, message = "提示词长度不能超过10000")
    private String prompt;

    private Long projectId;

    private Long userId;

    private BigDecimal temperature;

    private Integer maxTokens;
}
