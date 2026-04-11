package com.aimpl.domain.agent.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgentPerformanceVO {

    private String agentCode;
    private long totalDecisions;
    private double avgExecutionTimeMs;
    private long reviewedCount;
    private long approvedCount;
    private long modifiedCount;
    private long rejectedCount;
    private BigDecimal approvalRate;
}
