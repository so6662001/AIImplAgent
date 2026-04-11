package com.aimpl.domain.workforce.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.aftersales.entity.AfterSalesTicket;
import com.aimpl.domain.aftersales.mapper.AfterSalesTicketMapper;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.workforce.entity.Engineer;
import com.aimpl.domain.workforce.entity.EngineerWorklog;
import com.aimpl.domain.workforce.entity.ProjectEvaluation;
import com.aimpl.domain.workforce.mapper.EngineerMapper;
import com.aimpl.domain.workforce.mapper.EngineerWorklogMapper;
import com.aimpl.domain.workforce.mapper.ProjectEvaluationMapper;
import com.aimpl.domain.workforce.vo.CompositeScoreVO;
import com.aimpl.domain.workforce.vo.CompositeScoreVO.DimensionScoreVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompositeScoreService {

    private final EngineerMapper engineerMapper;
    private final ProjectMapper projectMapper;
    private final ProjectEvaluationMapper projectEvaluationMapper;
    private final AfterSalesTicketMapper afterSalesTicketMapper;
    private final EngineerWorklogMapper engineerWorklogMapper;

    @Transactional
    public CompositeScoreVO calculateCompositeScore(Long engineerId) {
        Engineer engineer = engineerMapper.selectById(engineerId);
        if (engineer == null) {
            throw new BizException("工程师不存在: " + engineerId);
        }

        BigDecimal dim1 = calculateDeliveryQuality(engineerId);
        BigDecimal dim2 = calculateCustomerSatisfaction(engineerId);
        BigDecimal dim3 = calculateWorkEfficiency(engineer);
        BigDecimal dim4 = calculateProcessCompliance(engineerId);
        BigDecimal dim5 = BigDecimal.valueOf(75);
        BigDecimal dim6 = BigDecimal.valueOf(70);

        BigDecimal composite = dim1.multiply(new BigDecimal("0.35"))
                .add(dim2.multiply(new BigDecimal("0.20")))
                .add(dim3.multiply(new BigDecimal("0.15")))
                .add(dim4.multiply(new BigDecimal("0.10")))
                .add(dim5.multiply(new BigDecimal("0.10")))
                .add(dim6.multiply(new BigDecimal("0.10")))
                .setScale(2, RoundingMode.HALF_UP);

        engineer.setCompositeScore(composite);
        engineerMapper.updateById(engineer);

        CompositeScoreVO vo = new CompositeScoreVO();
        vo.setEngineerId(engineerId);
        vo.setName(engineer.getName());
        vo.setCompositeScore(composite);
        vo.setRating(determineRating(composite));

        List<DimensionScoreVO> dims = new ArrayList<>();
        dims.add(buildDimension("项目交付质量", new BigDecimal("0.35"), dim1, "项目评价PQI加权平均"));
        dims.add(buildDimension("客户满意度", new BigDecimal("0.20"), dim2, "售后工单客户满意度评分"));
        dims.add(buildDimension("工作效率", new BigDecimal("0.15"), dim3, "基于月度空闲率计算"));
        dims.add(buildDimension("过程规范", new BigDecimal("0.10"), dim4, "日报提交率"));
        dims.add(buildDimension("团队协作", new BigDecimal("0.10"), dim5, "默认值(暂无同行评审系统)"));
        dims.add(buildDimension("成长与贡献", new BigDecimal("0.10"), dim6, "默认值(暂无知识库贡献追踪)"));
        vo.setDimensions(dims);

        vo.setGrowthSuggestions(generateSuggestions(dim1, dim2, dim3, dim4, composite, engineerId));

        return vo;
    }

    private BigDecimal calculateDeliveryQuality(Long engineerId) {
        List<Long> projectIds = projectMapper.selectList(
                new LambdaQueryWrapper<Project>().eq(Project::getPmId, engineerId)
        ).stream().map(Project::getId).collect(Collectors.toList());

        if (projectIds.isEmpty()) {
            return BigDecimal.valueOf(70);
        }

        List<ProjectEvaluation> evaluations = projectEvaluationMapper.selectList(
                new LambdaQueryWrapper<ProjectEvaluation>()
                        .in(ProjectEvaluation::getProjectId, projectIds)
                        .orderByAsc(ProjectEvaluation::getCreateTime)
        );

        if (evaluations.isEmpty()) {
            return BigDecimal.valueOf(70);
        }

        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        BigDecimal weightedSum = BigDecimal.ZERO;
        BigDecimal weightTotal = BigDecimal.ZERO;

        for (ProjectEvaluation eval : evaluations) {
            if (eval.getPqiScore() == null) continue;

            BigDecimal timeWeight = (eval.getCreateTime() != null && eval.getCreateTime().isAfter(threeMonthsAgo))
                    ? new BigDecimal("1.5")
                    : BigDecimal.ONE;

            weightedSum = weightedSum.add(eval.getPqiScore().multiply(timeWeight));
            weightTotal = weightTotal.add(timeWeight);
        }

        if (weightTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(70);
        }

        return weightedSum.divide(weightTotal, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCustomerSatisfaction(Long engineerId) {
        List<AfterSalesTicket> tickets = afterSalesTicketMapper.selectList(
                new LambdaQueryWrapper<AfterSalesTicket>()
                        .eq(AfterSalesTicket::getAssignedEngineerId, engineerId)
                        .isNotNull(AfterSalesTicket::getCustomerSatisfaction)
        );

        if (tickets.isEmpty()) {
            return BigDecimal.valueOf(70);
        }

        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (AfterSalesTicket ticket : tickets) {
            if (ticket.getCustomerSatisfaction() != null) {
                sum = sum.add(BigDecimal.valueOf(ticket.getCustomerSatisfaction()));
                count++;
            }
        }

        if (count == 0) {
            return BigDecimal.valueOf(70);
        }

        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(20))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateWorkEfficiency(Engineer engineer) {
        if (engineer.getMonthlyIdleRate() == null) {
            return BigDecimal.valueOf(75);
        }
        double idleRate = engineer.getMonthlyIdleRate().doubleValue();
        if (idleRate <= 0.1) return BigDecimal.valueOf(90);
        if (idleRate <= 0.2) return BigDecimal.valueOf(85);
        if (idleRate <= 0.3) return BigDecimal.valueOf(75);
        return BigDecimal.valueOf(60);
    }

    private BigDecimal calculateProcessCompliance(Long engineerId) {
        long total = engineerWorklogMapper.selectCount(
                new LambdaQueryWrapper<EngineerWorklog>()
                        .eq(EngineerWorklog::getEngineerId, engineerId)
        );

        if (total == 0) {
            return BigDecimal.valueOf(70);
        }

        long submitted = engineerWorklogMapper.selectCount(
                new LambdaQueryWrapper<EngineerWorklog>()
                        .eq(EngineerWorklog::getEngineerId, engineerId)
                        .eq(EngineerWorklog::getStatus, "SUBMITTED")
        );

        return BigDecimal.valueOf(submitted)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private List<String> generateSuggestions(BigDecimal dim1, BigDecimal dim2, BigDecimal dim3,
                                              BigDecimal dim4, BigDecimal composite, Long engineerId) {
        List<String> suggestions = new ArrayList<>();

        if (dim1.compareTo(BigDecimal.valueOf(70)) < 0) {
            suggestions.add("建议提升项目交付质量，关注里程碑按时达成和数据导入一次通过率");
        }
        if (dim2.compareTo(BigDecimal.valueOf(70)) < 0) {
            suggestions.add("建议加强客户沟通，关注售后工单处理时效和客户满意度");
        }
        if (dim3.compareTo(BigDecimal.valueOf(70)) < 0) {
            suggestions.add("建议提升工作效率，减少空闲时间");
        }
        if (dim4.compareTo(BigDecimal.valueOf(70)) < 0) {
            suggestions.add("建议提高日报提交率，确保工作过程规范");
        }

        if (composite.compareTo(BigDecimal.valueOf(85)) >= 0) {
            List<Long> projectIds = projectMapper.selectList(
                    new LambdaQueryWrapper<Project>().eq(Project::getPmId, engineerId)
            ).stream().map(Project::getId).collect(Collectors.toList());

            if (projectIds.size() >= 3) {
                suggestions.add("综合表现优秀，建议进入晋升评估通道");
            }
        }

        return suggestions;
    }

    private DimensionScoreVO buildDimension(String name, BigDecimal weight, BigDecimal score, String source) {
        DimensionScoreVO d = new DimensionScoreVO();
        d.setDimensionName(name);
        d.setWeight(weight);
        d.setScore(score);
        d.setSource(source);
        return d;
    }

    private String determineRating(BigDecimal score) {
        double s = score.doubleValue();
        if (s >= 90) return "★★★★★卓越";
        if (s >= 80) return "★★★★☆优良";
        if (s >= 60) return "★★★☆☆合格";
        if (s >= 40) return "★★☆☆☆待改进";
        return "★☆☆☆☆不合格";
    }
}
