package com.aimpl.domain.analytics.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepartmentAnalyticsVO {

    private int activeProjects;
    private int completedProjects;
    private int totalEngineers;
    private BigDecimal avgUtilizationRate;
    private BigDecimal avgPqi;
    private int totalTickets;
    private BigDecimal ticketResolutionRate;
    private int knowledgeEntryCount;
}
