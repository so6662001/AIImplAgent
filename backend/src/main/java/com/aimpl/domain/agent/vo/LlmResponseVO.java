package com.aimpl.domain.agent.vo;

import lombok.Data;

@Data
public class LlmResponseVO {

    private String agentCode;
    private String providerName;
    private String modelName;
    private String response;
    private int tokensUsed;
    private long executionTimeMs;
    private boolean fallbackUsed;
    private Long decisionLogId;
}
