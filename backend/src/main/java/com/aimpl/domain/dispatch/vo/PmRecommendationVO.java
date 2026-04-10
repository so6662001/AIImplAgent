package com.aimpl.domain.dispatch.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PmRecommendationVO {

    private Long engineerId;
    private String engineerCode;
    private String name;
    private String level;
    private int matchScore;
    private List<String> matchReasons;
    private String riskNotes;
    private String currentStatus;
    private int currentProjectCount;
    private BigDecimal compositeScore;
}
