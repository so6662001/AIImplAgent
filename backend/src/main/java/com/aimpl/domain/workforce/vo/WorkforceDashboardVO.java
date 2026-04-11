package com.aimpl.domain.workforce.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class WorkforceDashboardVO {

    private int totalEngineers;
    private int onProjectCount;
    private int idleCount;
    private int trainingCount;
    private int leaveCount;
    private BigDecimal utilizationRate;
    private BigDecimal idleRate;
    private BigDecimal avgCompositeScore;
    private List<EngineerStatusRowVO> engineers;

    @Data
    public static class EngineerStatusRowVO {
        private Long engineerId;
        private String name;
        private String level;
        private String currentStatus;
        private String currentProjectName;
        private LocalDate expectedRelease;
        private BigDecimal compositeScore;
        private int monthlyProjectCount;
        private BigDecimal monthlyIdleRate;
    }
}
