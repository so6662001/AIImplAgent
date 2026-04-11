package com.aimpl.domain.report.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GeneratedDeliveryReportVO {

    private Long reportId;

    private Long projectId;

    private String projectCode;

    private String customerName;

    private LocalDateTime generatedAt;

    private BigDecimal overallScore;

    private String overallStatus;

    private List<DeliveryReportSectionVO> sections;

    private AutoCollectedMetricsVO metrics;
}
