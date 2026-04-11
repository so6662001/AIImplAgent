package com.aimpl.domain.analytics.service;

import com.aimpl.common.enums.EngineerStatus;
import com.aimpl.common.enums.ProjectStatus;
import com.aimpl.common.enums.SimulationStatus;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.analytics.vo.DepartmentAnalyticsVO;
import com.aimpl.domain.analytics.vo.ProjectAnalyticsVO;
import com.aimpl.domain.analytics.vo.ProjectAnalyticsVO.*;
import com.aimpl.domain.assist.entity.SupportTicket;
import com.aimpl.domain.assist.mapper.SupportTicketMapper;
import com.aimpl.domain.dataimport.entity.ImportProgress;
import com.aimpl.domain.dataimport.mapper.ImportProgressMapper;
import com.aimpl.domain.knowledge.entity.KnowledgeEntry;
import com.aimpl.domain.knowledge.mapper.KnowledgeEntryMapper;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.simulation.entity.SimulationScene;
import com.aimpl.domain.simulation.mapper.SimulationSceneMapper;
import com.aimpl.domain.training.entity.ExamRecord;
import com.aimpl.domain.training.entity.TraineeProfile;
import com.aimpl.domain.training.mapper.ExamRecordMapper;
import com.aimpl.domain.training.mapper.TraineeProfileMapper;
import com.aimpl.domain.training.service.ExamService;
import com.aimpl.domain.workforce.entity.Engineer;
import com.aimpl.domain.workforce.mapper.EngineerMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final Set<ProjectStatus> ACTIVE_STATUSES = Set.of(
            ProjectStatus.RESEARCH, ProjectStatus.PLAN, ProjectStatus.TRAINING,
            ProjectStatus.DATA_IMPORT, ProjectStatus.GO_LIVE, ProjectStatus.FOLLOW_UP
    );
    private static final Set<ProjectStatus> COMPLETED_STATUSES = Set.of(
            ProjectStatus.DELIVERED, ProjectStatus.AFTER_SALES
    );

    private final ProjectMapper projectMapper;
    private final TraineeProfileMapper traineeProfileMapper;
    private final ExamRecordMapper examRecordMapper;
    private final ExamService examService;
    private final ImportProgressMapper importProgressMapper;
    private final SupportTicketMapper supportTicketMapper;
    private final SimulationSceneMapper simulationSceneMapper;
    private final EngineerMapper engineerMapper;
    private final KnowledgeEntryMapper knowledgeEntryMapper;

    public ProjectAnalyticsVO getProjectAnalytics(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        ProjectAnalyticsVO vo = new ProjectAnalyticsVO();
        vo.setProjectId(projectId);
        vo.setProjectCode(project.getProjectCode());
        vo.setCustomerName(project.getCustomerName());
        vo.setStatus(project.getStatus() != null ? project.getStatus().getLabel() : "未知");

        vo.setTrainingMetrics(buildTrainingMetrics(projectId));
        vo.setImportMetrics(buildImportMetrics(projectId));
        vo.setTicketMetrics(buildTicketMetrics(projectId));
        vo.setSimulationMetrics(buildSimulationMetrics(projectId));

        int healthScore = calculateHealthScore(vo);
        vo.setOverallHealthScore(healthScore);
        vo.setHealthLevel(determineHealthLevel(healthScore));

        return vo;
    }

    public DepartmentAnalyticsVO getDepartmentAnalytics() {
        DepartmentAnalyticsVO vo = new DepartmentAnalyticsVO();

        List<Project> allProjects = projectMapper.selectList(null);
        vo.setActiveProjects((int) allProjects.stream()
                .filter(p -> p.getStatus() != null && ACTIVE_STATUSES.contains(p.getStatus()))
                .count());
        vo.setCompletedProjects((int) allProjects.stream()
                .filter(p -> p.getStatus() != null && COMPLETED_STATUSES.contains(p.getStatus()))
                .count());

        List<Engineer> engineers = engineerMapper.selectList(null);
        vo.setTotalEngineers(engineers.size());

        if (engineers.isEmpty()) {
            vo.setAvgUtilizationRate(BigDecimal.ZERO);
        } else {
            long onProject = engineers.stream()
                    .filter(e -> e.getCurrentStatus() == EngineerStatus.ON_PROJECT)
                    .count();
            vo.setAvgUtilizationRate(BigDecimal.valueOf(onProject * 100.0 / engineers.size())
                    .setScale(2, RoundingMode.HALF_UP));
        }

        BigDecimal scoreSum = BigDecimal.ZERO;
        int scoreCount = 0;
        for (Engineer e : engineers) {
            if (e.getCompositeScore() != null) {
                scoreSum = scoreSum.add(e.getCompositeScore());
                scoreCount++;
            }
        }
        vo.setAvgPqi(scoreCount > 0
                ? scoreSum.divide(BigDecimal.valueOf(scoreCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        List<SupportTicket> allTickets = supportTicketMapper.selectList(null);
        vo.setTotalTickets(allTickets.size());
        long resolvedTickets = allTickets.stream()
                .filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus())
                        || "CLOSED".equalsIgnoreCase(t.getStatus()))
                .count();
        vo.setTicketResolutionRate(allTickets.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(resolvedTickets * 100.0 / allTickets.size())
                        .setScale(2, RoundingMode.HALF_UP));

        long knowledgeCount = knowledgeEntryMapper.selectCount(
                new LambdaQueryWrapper<KnowledgeEntry>()
                        .eq(KnowledgeEntry::getEnabled, true));
        vo.setKnowledgeEntryCount((int) knowledgeCount);

        return vo;
    }

    private TrainingMetrics buildTrainingMetrics(Long projectId) {
        TrainingMetrics m = new TrainingMetrics();

        List<TraineeProfile> trainees = traineeProfileMapper.selectList(
                new LambdaQueryWrapper<TraineeProfile>()
                        .eq(TraineeProfile::getProjectId, projectId));
        m.setTraineeCount(trainees.size());

        List<ExamRecord> exams = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getProjectId, projectId));
        if (exams.isEmpty()) {
            m.setPassRate(BigDecimal.ZERO);
        } else {
            long passed = exams.stream().filter(ExamRecord::getPassed).count();
            m.setPassRate(BigDecimal.valueOf(passed * 100.0 / exams.size())
                    .setScale(2, RoundingMode.HALF_UP));
        }

        try {
            m.setKaReady(examService.checkGoLiveReadiness(projectId));
        } catch (BizException e) {
            m.setKaReady(false);
        }

        return m;
    }

    private ImportMetrics buildImportMetrics(Long projectId) {
        ImportMetrics m = new ImportMetrics();
        m.setTotal(22);

        ImportProgress progress = importProgressMapper.selectOne(
                new LambdaQueryWrapper<ImportProgress>()
                        .eq(ImportProgress::getProjectId, projectId));
        if (progress != null) {
            int completedBatches = 0;
            if ("COMPLETED".equals(progress.getBatch1Status())) completedBatches++;
            if ("COMPLETED".equals(progress.getBatch2Status())) completedBatches++;
            if ("COMPLETED".equals(progress.getBatch3Status())) completedBatches++;
            if ("COMPLETED".equals(progress.getBatch4Status())) completedBatches++;
            if ("COMPLETED".equals(progress.getBatch5Status())) completedBatches++;
            m.setCompleted(completedBatches);
            m.setRate(BigDecimal.valueOf(completedBatches * 100.0 / 5)
                    .setScale(2, RoundingMode.HALF_UP));
        } else {
            m.setCompleted(0);
            m.setRate(BigDecimal.ZERO);
        }

        return m;
    }

    private TicketMetrics buildTicketMetrics(Long projectId) {
        TicketMetrics m = new TicketMetrics();

        List<SupportTicket> tickets = supportTicketMapper.selectList(
                new LambdaQueryWrapper<SupportTicket>()
                        .eq(SupportTicket::getProjectId, projectId));
        m.setTotal(tickets.size());

        int resolved = (int) tickets.stream()
                .filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus())
                        || "CLOSED".equalsIgnoreCase(t.getStatus()))
                .count();
        m.setResolved(resolved);
        m.setRate(tickets.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(resolved * 100.0 / tickets.size())
                        .setScale(2, RoundingMode.HALF_UP));

        return m;
    }

    private SimulationMetrics buildSimulationMetrics(Long projectId) {
        SimulationMetrics m = new SimulationMetrics();

        List<SimulationScene> scenes = simulationSceneMapper.selectList(
                new LambdaQueryWrapper<SimulationScene>()
                        .eq(SimulationScene::getProjectId, projectId));
        m.setTotal(scenes.size());

        int passed = (int) scenes.stream()
                .filter(s -> SimulationStatus.PASSED.equals(s.getStatus()))
                .count();
        m.setPassed(passed);
        m.setRate(scenes.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(passed * 100.0 / scenes.size())
                        .setScale(2, RoundingMode.HALF_UP));

        return m;
    }

    private int calculateHealthScore(ProjectAnalyticsVO vo) {
        int score = 0;
        int weightSum = 0;

        TrainingMetrics tm = vo.getTrainingMetrics();
        if (tm.getTraineeCount() > 0) {
            score += tm.getPassRate().intValue() * 25 / 100;
            weightSum += 25;
            if (tm.isKaReady()) score += 5;
            weightSum += 5;
        }

        ImportMetrics im = vo.getImportMetrics();
        score += im.getRate().intValue() * 20 / 100;
        weightSum += 20;

        TicketMetrics tkm = vo.getTicketMetrics();
        if (tkm.getTotal() > 0) {
            score += tkm.getRate().intValue() * 15 / 100;
            weightSum += 15;
        }

        SimulationMetrics sm = vo.getSimulationMetrics();
        if (sm.getTotal() > 0) {
            score += sm.getRate().intValue() * 15 / 100;
            weightSum += 15;
        }

        if (weightSum == 0) {
            return 50;
        }

        return Math.min(100, score * 100 / weightSum);
    }

    private String determineHealthLevel(int score) {
        if (score >= 90) return "优秀";
        if (score >= 75) return "良好";
        if (score >= 60) return "一般";
        return "需关注";
    }
}
