package com.aimpl.domain.training.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DayDocumentStatusVO {
    private LocalDate date;
    private boolean planUploaded;
    private boolean coursewareUploaded;
    private boolean signInCompleted;
    private boolean summaryUploaded;
    private boolean examConducted;
    private boolean dailyReportSubmitted;
    private BigDecimal completionRate;
}
