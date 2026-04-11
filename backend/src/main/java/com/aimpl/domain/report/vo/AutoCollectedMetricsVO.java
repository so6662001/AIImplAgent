package com.aimpl.domain.report.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AutoCollectedMetricsVO {

    private BigDecimal trainingPassRate;

    private Integer traineeCount;

    private Integer kaCount;

    private BigDecimal examPassRate;

    private BigDecimal worklogSubmissionRate;

    private BigDecimal ticketResolutionRate;

    private BigDecimal dataImportCompletionRate;

    private BigDecimal documentCompletionRate;

    private Integer totalExams;

    private Integer passedExams;

    private Integer failedExams;

    private Integer totalTickets;

    private Integer resolvedTickets;

    private Integer openTickets;

    private Integer totalWorklogs;

    private Integer submittedWorklogs;

    private Integer trainingDays;

    private Boolean goLiveReady;

    private BigDecimal simulationPassRate;
}
