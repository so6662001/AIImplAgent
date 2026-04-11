package com.aimpl.domain.workforce.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class MonthlyWorkforceReportVO {

    private LocalDate reportMonth;
    private int totalEngineers;
    private BigDecimal avgCompositeScore;
    private BigDecimal avgIdleRate;
    private List<EngineerRankingVO> rankings;
    private List<ProjectEvalSummaryVO> recentEvaluations;
    private IdleRateDistributionVO idleDistribution;
    private TalentOverviewVO talentOverview;

    @Data
    public static class EngineerRankingVO {
        private int rank;
        private String name;
        private String level;
        private int monthlyProjectCount;
        private BigDecimal compositeScore;
        private BigDecimal monthlyIdleRate;
        private String trend;
    }

    @Data
    public static class ProjectEvalSummaryVO {
        private Long evaluationId;
        private Long projectId;
        private String projectCode;
        private BigDecimal pqiScore;
        private String rating;
        private java.time.LocalDateTime createTime;
    }

    @Data
    public static class IdleRateDistributionVO {
        private int under10;
        private int range10to20;
        private int range20to30;
        private int range30to50;
        private int over50;
    }

    @Data
    public static class TalentOverviewVO {
        private List<String> promotionCandidates;
        private List<String> keyTrainingTargets;
        private List<String> needAttention;
        private List<String> skillGaps;
    }
}
