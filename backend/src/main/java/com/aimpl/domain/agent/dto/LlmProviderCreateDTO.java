package com.aimpl.domain.agent.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LlmProviderCreateDTO {

    @NotBlank(message = "供应商名称不能为空")
    @Size(max = 30, message = "供应商名称长度不能超过30")
    private String providerName;

    @NotBlank(message = "供应商类型不能为空")
    private String providerType;

    @NotBlank(message = "API端点不能为空")
    private String apiEndpoint;

    @NotBlank(message = "API密钥不能为空")
    private String apiKey;

    @NotBlank(message = "模型名称不能为空")
    @Size(max = 50, message = "模型名称长度不能超过50")
    private String modelName;

    @Min(value = 100, message = "最大Token数不能小于100")
    @Max(value = 128000, message = "最大Token数不能超过128000")
    private Integer maxTokens;

    @DecimalMin(value = "0", message = "温度不能小于0")
    @DecimalMax(value = "2", message = "温度不能超过2")
    private BigDecimal temperature;
}
