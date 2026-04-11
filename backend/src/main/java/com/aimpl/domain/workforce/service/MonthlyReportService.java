package com.aimpl.domain.workforce.service;

import com.aimpl.common.enums.EngineerStatus;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.workforce.entity.Engineer;
import com.aimpl.domain.workforce.entity.ProjectEvaluation;
import com.aimpl.domain.workforce.mapper.EngineerMapper;
import com.aimpl.domain.workforce.mapper.ProjectEvaluationMapper;
import com.aimpl.domain.workforce.vo.MonthlyWorkforceReportVO;
import com.aimpl.domain.workforce.vo.MonthlyWorkforceReportVO.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthlyReportService {

    private final EngineerMapper engineerMapper;
    private final ProjectEvaluationMapper projectEvaluationMapper;
    private final ProjectMapper projectMapper;

    public MonthlyWorkforceReportVO generateMonthlyReport() {
        List<Engineer> allEngineers = engineerMapper.selectList(
                new LambdaQueryWrapper<Engineer>().orderByDesc(Engineer::getCompositeScore)
        );

        MonthlyWorkforceReportVO report = new MonthlyWorkforceReportVO();
        report.setReportMonth(LocalDate.now().withDayOfMonth(1));
        report.setTotalEngineers(allEngineers.size());

        BigDecimal scoreSum = BigDecimal.ZERO;
        BigDecimal idleSum = BigDecimal.ZERO;
        int scoreCount = 0;
        int idleCount = 0;

        for (Engineer e : allEngineers) {
            if (e.getCompositeScore() != null) {
                scoreSum = scoreSum.add(e.getCompositeScore());
                scoreCount++;
            }
            if (e.getMonthlyIdleRate() != null) {
                idleSum = idleSum.add(e.getMonthlyIdleRate());
                idleCount++;
            }
        }

        report.setAvgCompositeScore(scoreCount > 0
                ? scoreSum.divide(BigDecimal.valueOf(scoreCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        report.setAvgIdleRate(idleCount > 0
                ? idleSum.divide(BigDecimal.valueOf(idleCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        report.setRankings(buildRankings(allEngineers));
        report.setRecentEvaluations(buildRecentEvaluations());
        report.setIdleDistribution(buildIdleDistribution(allEngineers));
        report.setTalentOverview(buildTalentOverview(allEngineers));

        return report;
    }

    private List<EngineerRankingVO> buildRankings(List<Engineer> engineers) {
        List<EngineerRankingVO> rankings = new ArrayList<>();
        int rank = 1;
        for (Engineer e : engineers) {
            EngineerRankingVO r = new EngineerRankingVO();
            r.setRank(rank++);
            r.setName(e.getName());
            r.setLevel(e.getLevel() != null ? e.getLevel().getLabel() : null);
            r.setMonthlyProjectCount(e.getMonthlyProjectCount() != null ? e.getMonthlyProjectCount() : 0);
            r.setCompositeScore(e.getCompositeScore());
            r.setMonthlyIdleRate(e.getMonthlyIdleRate());
            r.setTrend("→");
            rankings.add(r);
        }
        return rankings;
    }

    private List<ProjectEvalSummaryVO> buildRecentEvaluations() {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<ProjectEvaluation> evals = projectEvaluationMapper.selectList(
                new LambdaQueryWrapper<ProjectEvaluation>()
                        .ge(ProjectEvaluation::getCreateTime, monthStart)
                        .orderByDesc(ProjectEvaluation::getCreateTime)
        );

        List<ProjectEvalSummaryVO> summaries = new ArrayList<>();
        for (ProjectEvaluation eval : evals) {
            ProjectEvalSummaryVO s = new ProjectEvalSummaryVO();
            s.setEvaluationId(eval.getId());
            s.setProjectId(eval.getProjectId());
            s.setPqiScore(eval.getPqiScore());
            s.setRating(eval.getRating() != null ? eval.getRating().name() : null);
            s.setCreateTime(eval.getCreateTime());

            Project project = projectMapper.selectById(eval.getProjectId());
            if (project != null) {
                s.setProjectCode(project.getProjectCode());
            }
            summaries.add(s);
        }
        return summaries;
    }

    private IdleRateDistributionVO buildIdleDistribution(List<Engineer> engineers) {
        IdleRateDistributionVO dist = new IdleRateDistributionVO();
        for (Engineer e : engineers) {
            BigDecimal rate = e.getMonthlyIdleRate();
            if (rate == null) {
                dist.setUnder10(dist.getUnder10() + 1);
                continue;
            }
            double r = rate.doubleValue();
            if (r < 0.10) dist.setUnder10(dist.getUnder10() + 1);
            else if (r < 0.20) dist.setRange10to20(dist.getRange10to20() + 1);
            else if (r < 0.30) dist.setRange20to30(dist.getRange20to30() + 1);
            else if (r < 0.50) dist.setRange30to50(dist.getRange30to50() + 1);
            else dist.setOver50(dist.getOver50() + 1);
        }
        return dist;
    }

    private TalentOverviewVO buildTalentOverview(List<Engineer> engineers) {
        TalentOverviewVO talent = new TalentOverviewVO();
        List<String> promotion = new ArrayList<>();
        List<String> keyTraining = new ArrayList<>();
        List<String> needAttention = new ArrayList<>();

        for (Engineer e : engineers) {
            BigDecimal score = e.getCompositeScore();
            if (score == null) {
                needAttention.add(e.getName());
                continue;
            }
            double s = score.doubleValue();
            if (s >= 85) promotion.add(e.getName());
            else if (s >= 70) keyTraining.add(e.getName());
            else needAttention.add(e.getName());
        }

        talent.setPromotionCandidates(promotion);
        talent.setKeyTrainingTargets(keyTraining);
        talent.setNeedAttention(needAttention);
        talent.setSkillGaps(identifySkillGaps(engineers));

        return talent;
    }

    private List<String> identifySkillGaps(List<Engineer> engineers) {
        Map<String, Integer> skillCount = new HashMap<>();
        for (Engineer e : engineers) {
            if (e.getSkills() == null || e.getSkills().isBlank()) continue;
            String[] skills = e.getSkills().split("[,，;；\\s]+");
            for (String skill : skills) {
                String trimmed = skill.trim();
                if (!trimmed.isEmpty()) {
                    skillCount.merge(trimmed, 1, Integer::sum);
                }
            }
        }

        int total = engineers.size();
        int threshold = Math.max(1, total / 5);

        return skillCount.entrySet().stream()
                .filter(entry -> entry.getValue() <= threshold)
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
