package com.aimpl.domain.training.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TrainingDashboardDTO {

    private Long projectId;
    private int totalTrainees;
    private int kaUserCount;
    private BigDecimal overallPassRate;
    private List<ModuleStat> moduleStats;
    private BigDecimal attendanceRate;
    private BigDecimal documentCompletionRate;
    private boolean goLiveReady;

    @Data
    public static class ModuleStat {
        private String module;
        private int examCount;
        private BigDecimal passRate;

        public ModuleStat(String module, int examCount, BigDecimal passRate) {
            this.module = module;
            this.examCount = examCount;
            this.passRate = passRate;
        }
    }
}
