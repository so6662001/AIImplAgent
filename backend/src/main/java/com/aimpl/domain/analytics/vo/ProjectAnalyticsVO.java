package com.aimpl.domain.analytics.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProjectAnalyticsVO {

    private Long projectId;
    private String projectCode;
    private String customerName;
    private String status;

    private TrainingMetrics trainingMetrics;
    private ImportMetrics importMetrics;
    private TicketMetrics ticketMetrics;
    private SimulationMetrics simulationMetrics;

    private int overallHealthScore;
    private String healthLevel;

    @Data
    public static class TrainingMetrics {
        private int traineeCount;
        private BigDecimal passRate;
        private boolean kaReady;
    }

    @Data
    public static class ImportMetrics {
        private int completed;
        private int total;
        private BigDecimal rate;
    }

    @Data
    public static class TicketMetrics {
        private int total;
        private int resolved;
        private BigDecimal rate;
    }

    @Data
    public static class SimulationMetrics {
        private int total;
        private int passed;
        private BigDecimal rate;
    }
}
