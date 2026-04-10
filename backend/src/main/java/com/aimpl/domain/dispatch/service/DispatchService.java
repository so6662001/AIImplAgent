package com.aimpl.domain.dispatch.service;

import com.aimpl.common.enums.EngineerLevel;
import com.aimpl.common.enums.EngineerStatus;
import com.aimpl.common.enums.IndustryType;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.dispatch.dto.DispatchAssignDTO;
import com.aimpl.domain.dispatch.dto.DispatchRequestDTO;
import com.aimpl.domain.dispatch.vo.DispatchResultVO;
import com.aimpl.domain.dispatch.vo.PmRecommendationVO;
import com.aimpl.domain.dispatch.vo.ScheduleSuggestionVO;
import com.aimpl.domain.dispatch.vo.ScheduleSuggestionVO.MilestoneVO;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.workforce.entity.Engineer;
import com.aimpl.domain.workforce.mapper.EngineerMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DispatchService {

    private final ProjectMapper projectMapper;
    private final EngineerMapper engineerMapper;

    private static final Set<EngineerLevel> PM_LEVELS = Set.of(
            EngineerLevel.PM, EngineerLevel.SENIOR_PM, EngineerLevel.EXPERT_PM
    );

    public DispatchResultVO recommend(DispatchRequestDTO request) {
        ProjectInfo info = resolveProjectInfo(request);

        List<Engineer> candidates = engineerMapper.selectList(
                new LambdaQueryWrapper<Engineer>()
                        .in(Engineer::getLevel, PM_LEVELS)
        );

        List<PmRecommendationVO> scored = candidates.stream()
                .map(eng -> scoreCandidate(eng, info))
                .sorted(Comparator.comparingInt(PmRecommendationVO::getMatchScore).reversed())
                .limit(5)
                .collect(Collectors.toList());

        ScheduleSuggestionVO schedule = generateSchedule(info, scored);

        DispatchResultVO result = new DispatchResultVO();
        result.setRecommendedPms(scored);
        result.setScheduleSuggestion(schedule);
        return result;
    }

    @Transactional
    public Project assign(DispatchAssignDTO dto) {
        Project project = projectMapper.selectById(dto.getProjectId());
        if (project == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }

        Engineer engineer = engineerMapper.selectById(dto.getEngineerId());
        if (engineer == null) {
            throw new BizException("工程师不存在: " + dto.getEngineerId());
        }

        project.setPmId(dto.getEngineerId());
        projectMapper.updateById(project);

        engineer.setCurrentStatus(EngineerStatus.ON_PROJECT);
        engineer.setCurrentProjectId(dto.getProjectId());
        engineer.setMonthlyProjectCount(
                (engineer.getMonthlyProjectCount() != null ? engineer.getMonthlyProjectCount() : 0) + 1
        );
        engineerMapper.updateById(engineer);

        return project;
    }

    private PmRecommendationVO scoreCandidate(Engineer eng, ProjectInfo info) {
        int skillScore = calcSkillScore(eng, info);
        int availabilityScore = calcAvailabilityScore(eng);
        int performanceScore = calcPerformanceScore(eng);
        int workloadScore = calcWorkloadScore(eng);
        int totalScore = Math.min(skillScore + availabilityScore + performanceScore + workloadScore, 100);

        List<String> reasons = generateMatchReasons(eng, info, skillScore);
        String riskNotes = generateRiskNotes(eng);

        PmRecommendationVO vo = new PmRecommendationVO();
        vo.setEngineerId(eng.getId());
        vo.setEngineerCode(eng.getEngineerCode());
        vo.setName(eng.getName());
        vo.setLevel(eng.getLevel() != null ? eng.getLevel().getLabel() : null);
        vo.setMatchScore(totalScore);
        vo.setMatchReasons(reasons);
        vo.setRiskNotes(riskNotes);
        vo.setCurrentStatus(eng.getCurrentStatus() != null ? eng.getCurrentStatus().getLabel() : null);
        vo.setCurrentProjectCount(eng.getMonthlyProjectCount() != null ? eng.getMonthlyProjectCount() : 0);
        vo.setCompositeScore(eng.getCompositeScore() != null ? eng.getCompositeScore() : BigDecimal.ZERO);
        return vo;
    }

    private int calcSkillScore(Engineer eng, ProjectInfo info) {
        if (eng.getSkills() == null || eng.getSkills().isBlank()) {
            return 0;
        }

        Set<String> engineerSkills = Arrays.stream(eng.getSkills().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        int matchCount = 0;
        if (info.modules != null) {
            for (String module : info.modules) {
                if (engineerSkills.contains(module.trim())) {
                    matchCount++;
                }
            }
        }

        int score = Math.min(matchCount * 5, 35);

        if (info.industryType != null && engineerSkills.contains(info.industryType.getLabel())) {
            score = Math.min(score + 10, 35);
        }

        return score;
    }

    private int calcAvailabilityScore(Engineer eng) {
        if (eng.getCurrentStatus() == null) {
            return 0;
        }
        switch (eng.getCurrentStatus()) {
            case IDLE:
                return 25;
            case ON_PROJECT:
                int projectCount = eng.getMonthlyProjectCount() != null ? eng.getMonthlyProjectCount() : 0;
                return projectCount < 2 ? 15 : 5;
            case TRAINING:
            case LEAVE:
                return 0;
            default:
                return 0;
        }
    }

    private int calcPerformanceScore(Engineer eng) {
        if (eng.getCompositeScore() == null) {
            return 0;
        }
        return eng.getCompositeScore()
                .multiply(new BigDecimal("0.25"))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private int calcWorkloadScore(Engineer eng) {
        if (eng.getMonthlyIdleRate() == null) {
            return 10;
        }
        double idleRate = eng.getMonthlyIdleRate().doubleValue();
        if (idleRate > 0.3) return 15;
        if (idleRate >= 0.1) return 10;
        return 5;
    }

    private List<String> generateMatchReasons(Engineer eng, ProjectInfo info, int skillScore) {
        List<String> reasons = new ArrayList<>();

        if (skillScore > 0 && info.modules != null) {
            Set<String> engineerSkills = eng.getSkills() != null
                    ? Arrays.stream(eng.getSkills().split(",")).map(String::trim).collect(Collectors.toSet())
                    : Collections.emptySet();
            long matchCount = info.modules.stream()
                    .filter(m -> engineerSkills.contains(m.trim()))
                    .count();
            if (matchCount > 0) {
                reasons.add("具有" + matchCount + "个匹配模块的交付经验");
            }
        }

        if (eng.getCurrentStatus() == EngineerStatus.IDLE) {
            reasons.add("当前状态空闲，可立即启动");
        }

        if (eng.getCompositeScore() != null && eng.getCompositeScore().doubleValue() >= 80) {
            reasons.add("综合评分" + eng.getCompositeScore().setScale(0, RoundingMode.HALF_UP) + "分，交付质量优良");
        }

        if (eng.getMonthlyIdleRate() != null && eng.getMonthlyIdleRate().doubleValue() > 0.2) {
            reasons.add("当前负载率低，有充足余量");
        }

        return reasons;
    }

    private String generateRiskNotes(Engineer eng) {
        List<String> risks = new ArrayList<>();

        int projectCount = eng.getMonthlyProjectCount() != null ? eng.getMonthlyProjectCount() : 0;
        if (projectCount > 0) {
            risks.add("当前有" + projectCount + "个进行中项目，需关注时间冲突");
        }

        if (eng.getCurrentStatus() == EngineerStatus.TRAINING || eng.getCurrentStatus() == EngineerStatus.LEAVE) {
            risks.add("近期处于培训/请假状态");
        }

        return risks.isEmpty() ? null : String.join("; ", risks);
    }

    private ScheduleSuggestionVO generateSchedule(ProjectInfo info, List<PmRecommendationVO> candidates) {
        int baseDuration;
        String scale = info.scale != null ? info.scale : "中型";
        switch (scale) {
            case "大型":
                baseDuration = 90;
                break;
            case "小型":
                baseDuration = 30;
                break;
            default:
                baseDuration = 60;
                break;
        }

        int moduleCount = info.modules != null ? info.modules.size() : 0;
        if (moduleCount > 3) {
            baseDuration += (moduleCount - 3) * 15;
        }

        if (info.modules != null && info.modules.stream().anyMatch(m -> "MES".equalsIgnoreCase(m.trim()))) {
            baseDuration += 20;
        }

        LocalDate startDate = info.expectedStartDate;
        if (startDate == null) {
            startDate = LocalDate.now().plusDays(7);
        }

        int duration = baseDuration;
        List<MilestoneVO> milestones = new ArrayList<>();
        milestones.add(new MilestoneVO("调研完成", (int) Math.round(duration * 0.10), "客户画像+背景分析报告"));
        milestones.add(new MilestoneVO("计划确认", (int) Math.round(duration * 0.15), "交付计划+WBS"));
        milestones.add(new MilestoneVO("模拟演练通过", (int) Math.round(duration * 0.30), "模拟演练评估报告"));
        milestones.add(new MilestoneVO("培训完成", (int) Math.round(duration * 0.55), "全员考核通过"));
        milestones.add(new MilestoneVO("数据导入完成", (int) Math.round(duration * 0.70), "期初数据+帐套"));
        milestones.add(new MilestoneVO("正式上线", (int) Math.round(duration * 0.85), "系统启用"));
        milestones.add(new MilestoneVO("交付验收", duration, "交付成果报告"));

        ScheduleSuggestionVO schedule = new ScheduleSuggestionVO();
        schedule.setRecommendedStart(startDate);
        schedule.setEstimatedDurationDays(duration);
        schedule.setMilestones(milestones);
        return schedule;
    }

    private ProjectInfo resolveProjectInfo(DispatchRequestDTO request) {
        ProjectInfo info = new ProjectInfo();

        if (request.getProjectId() != null) {
            Project project = projectMapper.selectById(request.getProjectId());
            if (project == null) {
                throw new BizException("项目不存在: " + request.getProjectId());
            }
            info.customerName = project.getCustomerName();
            info.industryType = project.getIndustryType();
            info.scale = project.getScale();
            info.modules = project.getModules() != null
                    ? Arrays.asList(project.getModules().split(","))
                    : Collections.emptyList();
            info.specialRequirements = project.getSpecialRequirements();
            info.expectedStartDate = project.getStartDate();
            info.region = project.getRegion();
        } else {
            info.customerName = request.getCustomerName();
            info.industryType = request.getIndustryType();
            info.scale = request.getScale();
            info.modules = request.getModules();
            info.specialRequirements = request.getSpecialRequirements();
            info.expectedStartDate = request.getExpectedStartDate();
            info.region = request.getRegion();
        }

        return info;
    }

    private static class ProjectInfo {
        String customerName;
        IndustryType industryType;
        String scale;
        List<String> modules;
        String specialRequirements;
        LocalDate expectedStartDate;
        String region;
    }
}
