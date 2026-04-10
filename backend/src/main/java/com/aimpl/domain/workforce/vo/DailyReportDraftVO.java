package com.aimpl.domain.workforce.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyReportDraftVO {
    private Long engineerId;
    private Long projectId;
    private LocalDate date;
    private String summary;
    private String trainingSection;
    private String examSection;
    private String issuesSection;
    private String nextDayPlan;
}
