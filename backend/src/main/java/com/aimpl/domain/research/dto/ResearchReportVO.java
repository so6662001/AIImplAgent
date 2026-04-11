package com.aimpl.domain.research.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResearchReportVO {

    private Long profileId;

    private String companyName;

    private LocalDateTime generatedAt;

    private List<ReportSectionVO> sections;

    private String overallRiskLevel;
}
