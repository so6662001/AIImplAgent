package com.aimpl.domain.report.service;

import com.aimpl.common.enums.ProjectStatus;
import com.aimpl.common.enums.ReportStatus;
import com.aimpl.common.enums.ReportType;
import com.aimpl.common.enums.SimulationStatus;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.assist.entity.SupportTicket;
import com.aimpl.domain.assist.entity.SystemAlert;
import com.aimpl.domain.assist.mapper.SupportTicketMapper;
import com.aimpl.domain.assist.mapper.SystemAlertMapper;
import com.aimpl.domain.dataimport.entity.ImportProgress;
import com.aimpl.domain.dataimport.mapper.ImportProgressMapper;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.entity.ProjectPlan;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.project.mapper.ProjectPlanMapper;
import com.aimpl.domain.report.entity.DeliveryReport;
import com.aimpl.domain.report.mapper.DeliveryReportMapper;
import com.aimpl.domain.report.vo.AutoCollectedMetricsVO;
import com.aimpl.domain.report.vo.DeliveryReportSectionVO;
import com.aimpl.domain.report.vo.GeneratedDeliveryReportVO;
import com.aimpl.domain.research.entity.CustomerProfile;
import com.aimpl.domain.research.mapper.CustomerProfileMapper;
import com.aimpl.domain.simulation.entity.SimulationScene;
import com.aimpl.domain.simulation.mapper.SimulationSceneMapper;
import com.aimpl.domain.training.entity.ExamRecord;
import com.aimpl.domain.training.entity.TraineeProfile;
import com.aimpl.domain.training.entity.TrainingDailyLog;
import com.aimpl.domain.training.mapper.ExamRecordMapper;
import com.aimpl.domain.training.mapper.TraineeProfileMapper;
import com.aimpl.domain.training.mapper.TrainingDailyLogMapper;
import com.aimpl.domain.training.service.ExamService;
import com.aimpl.domain.workforce.entity.EngineerWorklog;
import com.aimpl.domain.workforce.mapper.EngineerWorklogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryReportGeneratorService {

    private static final String NO_DATA = "暂无数据";

    private final ProjectMapper projectMapper;
    private final ProjectPlanMapper projectPlanMapper;
    private final TraineeProfileMapper traineeProfileMapper;
    private final ExamRecordMapper examRecordMapper;
    private final TrainingDailyLogMapper trainingDailyLogMapper;
    private final EngineerWorklogMapper engineerWorklogMapper;
    private final SupportTicketMapper supportTicketMapper;
    private final SystemAlertMapper systemAlertMapper;
    private final CustomerProfileMapper customerProfileMapper;
    private final ImportProgressMapper importProgressMapper;
    private final SimulationSceneMapper simulationSceneMapper;
    private final DeliveryReportMapper deliveryReportMapper;
    private final ExamService examService;

    private final ObjectMapper objectMapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        return om;
    }

    @Transactional
    public GeneratedDeliveryReportVO autoGenerate(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        AutoCollectedMetricsVO metrics = collectMetrics(projectId);

        List<DeliveryReportSectionVO> sections = new ArrayList<>();
        sections.add(buildSection1ProjectOverview(project));
        sections.add(buildSection2DeliveryResults(project, metrics));
        sections.add(buildSection3GoalAnalysis(project, projectId));
        sections.add(buildSection4TrainingSummary(projectId, metrics));
        sections.add(buildSection5EngineerWorklogs(projectId, metrics));
        sections.add(buildSection6ProcessQuality(projectId, project, metrics));
        sections.add(buildSection7OutstandingIssues(projectId));
        sections.add(buildSection8MaintenancePlan(project));

        BigDecimal overallScore = calculateOverallScore(metrics);
        String overallStatus = determineOverallStatus(overallScore);

        DeliveryReport entity = new DeliveryReport();
        entity.setProjectId(projectId);
        entity.setReportType(ReportType.FINAL);
        entity.setTitle(project.getProjectCode() + " 交付报告");
        entity.setTrainingPassRate(metrics.getTrainingPassRate());
        entity.setDataImportCompletionRate(metrics.getDataImportCompletionRate());
        entity.setTotalIssues(metrics.getTotalTickets());
        entity.setResolvedIssues(metrics.getResolvedTickets());
        entity.setDocumentCompletionRate(metrics.getDocumentCompletionRate());
        entity.setOverallScore(overallScore);
        entity.setAiComment("系统自动生成交付报告，综合评分" + overallScore + "分，状态：" + overallStatus);
        entity.setAutoGenerated(true);
        entity.setStatus(ReportStatus.DRAFT);

        try {
            entity.setReportContent(objectMapper.writeValueAsString(sections));
        } catch (JsonProcessingException e) {
            entity.setReportContent("[]");
        }

        deliveryReportMapper.insert(entity);

        GeneratedDeliveryReportVO vo = new GeneratedDeliveryReportVO();
        vo.setReportId(entity.getId());
        vo.setProjectId(projectId);
        vo.setProjectCode(project.getProjectCode());
        vo.setCustomerName(project.getCustomerName());
        vo.setGeneratedAt(LocalDateTime.now());
        vo.setOverallScore(overallScore);
        vo.setOverallStatus(overallStatus);
        vo.setSections(sections);
        vo.setMetrics(metrics);
        return vo;
    }

    private AutoCollectedMetricsVO collectMetrics(Long projectId) {
        AutoCollectedMetricsVO m = new AutoCollectedMetricsVO();

        List<TraineeProfile> trainees = traineeProfileMapper.selectList(
                new LambdaQueryWrapper<TraineeProfile>()
                        .eq(TraineeProfile::getProjectId, projectId));
        m.setTraineeCount(trainees.size());
        m.setKaCount((int) trainees.stream()
                .filter(t -> Boolean.TRUE.equals(t.getKaUser())).count());

        List<ExamRecord> exams = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getProjectId, projectId));
        m.setTotalExams(exams.size());
        int passed = (int) exams.stream().filter(ExamRecord::getPassed).count();
        m.setPassedExams(passed);
        m.setFailedExams(exams.size() - passed);
        m.setExamPassRate(exams.isEmpty() ? BigDecimal.ZERO
                : pct(passed, exams.size()));
        m.setTrainingPassRate(m.getExamPassRate());

        List<TrainingDailyLog> logs = trainingDailyLogMapper.selectList(
                new LambdaQueryWrapper<TrainingDailyLog>()
                        .eq(TrainingDailyLog::getProjectId, projectId));
        m.setTrainingDays(logs.size());
        if (logs.isEmpty()) {
            m.setDocumentCompletionRate(BigDecimal.ZERO);
        } else {
            int totalFlags = logs.size() * 6;
            int completed = 0;
            for (TrainingDailyLog log : logs) {
                if (Boolean.TRUE.equals(log.getPlanUploaded())) completed++;
                if (Boolean.TRUE.equals(log.getSignInCompleted())) completed++;
                if (Boolean.TRUE.equals(log.getCoursewareUploaded())) completed++;
                if (Boolean.TRUE.equals(log.getSummaryUploaded())) completed++;
                if (Boolean.TRUE.equals(log.getExamConducted())) completed++;
                if (Boolean.TRUE.equals(log.getDailyReportSubmitted())) completed++;
            }
            m.setDocumentCompletionRate(pct(completed, totalFlags));
        }

        try {
            m.setGoLiveReady(examService.checkGoLiveReadiness(projectId));
        } catch (BizException e) {
            m.setGoLiveReady(false);
        }

        List<EngineerWorklog> worklogs = engineerWorklogMapper.selectList(
                new LambdaQueryWrapper<EngineerWorklog>()
                        .eq(EngineerWorklog::getProjectId, projectId));
        m.setTotalWorklogs(worklogs.size());
        int submitted = (int) worklogs.stream()
                .filter(w -> "SUBMITTED".equalsIgnoreCase(w.getStatus())
                        || "APPROVED".equalsIgnoreCase(w.getStatus()))
                .count();
        m.setSubmittedWorklogs(submitted);
        m.setWorklogSubmissionRate(worklogs.isEmpty() ? BigDecimal.ZERO
                : pct(submitted, worklogs.size()));

        List<SupportTicket> tickets = supportTicketMapper.selectList(
                new LambdaQueryWrapper<SupportTicket>()
                        .eq(SupportTicket::getProjectId, projectId));
        m.setTotalTickets(tickets.size());
        int resolved = (int) tickets.stream()
                .filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus())
                        || "CLOSED".equalsIgnoreCase(t.getStatus()))
                .count();
        m.setResolvedTickets(resolved);
        m.setOpenTickets(tickets.size() - resolved);
        m.setTicketResolutionRate(tickets.isEmpty() ? BigDecimal.ZERO
                : pct(resolved, tickets.size()));

        ImportProgress importProgress = importProgressMapper.selectOne(
                new LambdaQueryWrapper<ImportProgress>()
                        .eq(ImportProgress::getProjectId, projectId));
        if (importProgress != null) {
            int completedBatches = 0;
            if ("COMPLETED".equals(importProgress.getBatch1Status())) completedBatches++;
            if ("COMPLETED".equals(importProgress.getBatch2Status())) completedBatches++;
            if ("COMPLETED".equals(importProgress.getBatch3Status())) completedBatches++;
            if ("COMPLETED".equals(importProgress.getBatch4Status())) completedBatches++;
            if ("COMPLETED".equals(importProgress.getBatch5Status())) completedBatches++;
            m.setDataImportCompletionRate(pct(completedBatches, 5));
        } else {
            m.setDataImportCompletionRate(BigDecimal.ZERO);
        }

        List<SimulationScene> scenes = simulationSceneMapper.selectList(
                new LambdaQueryWrapper<SimulationScene>()
                        .eq(SimulationScene::getProjectId, projectId));
        if (!scenes.isEmpty()) {
            long passedScenes = scenes.stream()
                    .filter(s -> SimulationStatus.PASSED.equals(s.getStatus()))
                    .count();
            m.setSimulationPassRate(pct((int) passedScenes, scenes.size()));
        } else {
            m.setSimulationPassRate(BigDecimal.ZERO);
        }

        return m;
    }

    // === Section builders ===

    private DeliveryReportSectionVO buildSection1ProjectOverview(Project project) {
        DeliveryReportSectionVO section = newSection(1, "项目概况");

        StringBuilder sb = new StringBuilder();
        sb.append("项目").append(safe(project.getProjectCode()))
                .append("为").append(safe(project.getCustomerName()));

        if (project.getIndustryType() != null) {
            sb.append("（").append(project.getIndustryType().getLabel()).append("企业）");
        }

        sb.append("提供");
        if (project.getModules() != null && !project.getModules().isBlank()) {
            sb.append(project.getModules());
        } else {
            sb.append("ERP系统");
        }
        sb.append("模块的实施交付服务。");

        if (project.getStartDate() != null) {
            sb.append("项目启动日期：").append(project.getStartDate()).append("，");
        }
        if (project.getEndDate() != null) {
            sb.append("计划结束日期：").append(project.getEndDate()).append("。");
        }
        if (project.getStatus() != null) {
            sb.append("当前状态：").append(project.getStatus().getLabel()).append("。");
        }
        if (project.getRegion() != null && !project.getRegion().isBlank()) {
            sb.append("项目区域：").append(project.getRegion()).append("。");
        }

        ProjectPlan plan = projectPlanMapper.selectOne(
                new LambdaQueryWrapper<ProjectPlan>()
                        .eq(ProjectPlan::getProjectId, project.getId())
                        .last("LIMIT 1"));
        if (plan != null && plan.getMilestones() != null && !plan.getMilestones().isBlank()) {
            sb.append("\n里程碑规划：").append(plan.getMilestones());
        }

        section.setContent(sb.toString());

        List<String> highlights = new ArrayList<>();
        highlights.add("客户：" + safe(project.getCustomerName()));
        if (project.getIndustryType() != null) {
            highlights.add("行业：" + project.getIndustryType().getLabel());
        }
        if (project.getModules() != null) {
            highlights.add("模块：" + project.getModules());
        }
        section.setHighlights(highlights);

        Map<String, Object> dp = new LinkedHashMap<>();
        dp.put("projectCode", project.getProjectCode());
        dp.put("customerName", project.getCustomerName());
        dp.put("status", project.getStatus() != null ? project.getStatus().name() : null);
        dp.put("startDate", project.getStartDate());
        dp.put("endDate", project.getEndDate());
        section.setDataPoints(dp);

        return section;
    }

    private DeliveryReportSectionVO buildSection2DeliveryResults(Project project, AutoCollectedMetricsVO metrics) {
        DeliveryReportSectionVO section = newSection(2, "交付成果总结");

        StringBuilder sb = new StringBuilder();
        if (project.getModules() != null && !project.getModules().isBlank()) {
            sb.append("系统上线模块：").append(project.getModules()).append("。");
        } else {
            sb.append("系统上线模块：").append(NO_DATA).append("。");
        }

        sb.append("\n数据迁移完成进度：")
                .append(metrics.getDataImportCompletionRate()).append("%");
        sb.append("（5批次导入）。");

        if (metrics.getSimulationPassRate().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("\n模拟演练通过率：").append(metrics.getSimulationPassRate()).append("%。");
        }

        if (Boolean.TRUE.equals(metrics.getGoLiveReady())) {
            sb.append("\nKA用户上线准备度检查：通过。");
        } else {
            sb.append("\nKA用户上线准备度检查：未通过或暂无数据。");
        }

        section.setContent(sb.toString());

        List<String> highlights = new ArrayList<>();
        highlights.add("数据迁移完成率：" + metrics.getDataImportCompletionRate() + "%");
        if (metrics.getSimulationPassRate().compareTo(BigDecimal.ZERO) > 0) {
            highlights.add("模拟演练通过率：" + metrics.getSimulationPassRate() + "%");
        }
        section.setHighlights(highlights);

        Map<String, Object> dp = new LinkedHashMap<>();
        dp.put("modules", project.getModules());
        dp.put("dataImportCompletionRate", metrics.getDataImportCompletionRate());
        dp.put("simulationPassRate", metrics.getSimulationPassRate());
        dp.put("goLiveReady", metrics.getGoLiveReady());
        section.setDataPoints(dp);

        return section;
    }

    private DeliveryReportSectionVO buildSection3GoalAnalysis(Project project, Long projectId) {
        DeliveryReportSectionVO section = newSection(3, "目标达成分析");

        CustomerProfile profile = customerProfileMapper.selectOne(
                new LambdaQueryWrapper<CustomerProfile>()
                        .eq(CustomerProfile::getProjectId, projectId)
                        .last("LIMIT 1"));

        StringBuilder sb = new StringBuilder();
        Map<String, Object> dp = new LinkedHashMap<>();
        List<String> highlights = new ArrayList<>();

        if (profile == null) {
            sb.append(NO_DATA).append("（客户画像未创建）");
        } else {
            String statusLabel = assessGoalStatus(project.getStatus());

            appendGoalRow(sb, highlights, dp, "管理目标", profile.getManagementGoals(), statusLabel);
            appendGoalRow(sb, highlights, dp, "流程目标", profile.getProcessGoals(), statusLabel);
            appendGoalRow(sb, highlights, dp, "效率目标", profile.getEfficiencyGoals(), statusLabel);
            appendGoalRow(sb, highlights, dp, "风控目标", profile.getRiskControlGoals(), statusLabel);
        }

        section.setContent(sb.toString());
        section.setHighlights(highlights);
        section.setDataPoints(dp);
        return section;
    }

    private DeliveryReportSectionVO buildSection4TrainingSummary(Long projectId, AutoCollectedMetricsVO metrics) {
        DeliveryReportSectionVO section = newSection(4, "培训完成情况");

        StringBuilder sb = new StringBuilder();
        sb.append("培训学员总数：").append(metrics.getTraineeCount()).append("人");
        sb.append("，其中KA用户：").append(metrics.getKaCount()).append("人。\n");

        sb.append("考核情况：共").append(metrics.getTotalExams()).append("次考核，");
        sb.append("通过").append(metrics.getPassedExams()).append("次，");
        sb.append("未通过").append(metrics.getFailedExams()).append("次，");
        sb.append("整体通过率：").append(metrics.getExamPassRate()).append("%。\n");

        List<ExamRecord> exams = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getProjectId, projectId));
        Map<String, List<ExamRecord>> byModule = exams.stream()
                .collect(Collectors.groupingBy(ExamRecord::getModule));
        if (!byModule.isEmpty()) {
            sb.append("各模块通过率：\n");
            for (Map.Entry<String, List<ExamRecord>> entry : byModule.entrySet()) {
                long passCount = entry.getValue().stream().filter(ExamRecord::getPassed).count();
                BigDecimal rate = pct((int) passCount, entry.getValue().size());
                sb.append("  - ").append(entry.getKey()).append("：")
                        .append(rate).append("%（").append(entry.getValue().size()).append("次考核）\n");
            }
        }

        sb.append("培训天数：").append(metrics.getTrainingDays()).append("天。\n");
        sb.append("培训文档完成率：").append(metrics.getDocumentCompletionRate()).append("%。\n");

        if (Boolean.TRUE.equals(metrics.getGoLiveReady())) {
            sb.append("上线准备度检查：已通过。");
        } else {
            sb.append("上线准备度检查：未通过。");
        }

        section.setContent(sb.toString());

        List<String> highlights = new ArrayList<>();
        highlights.add("学员数：" + metrics.getTraineeCount() + "人（KA " + metrics.getKaCount() + "人）");
        highlights.add("考核通过率：" + metrics.getExamPassRate() + "%");
        highlights.add("文档完成率：" + metrics.getDocumentCompletionRate() + "%");
        section.setHighlights(highlights);

        Map<String, Object> dp = new LinkedHashMap<>();
        dp.put("traineeCount", metrics.getTraineeCount());
        dp.put("kaCount", metrics.getKaCount());
        dp.put("totalExams", metrics.getTotalExams());
        dp.put("passedExams", metrics.getPassedExams());
        dp.put("examPassRate", metrics.getExamPassRate());
        dp.put("trainingDays", metrics.getTrainingDays());
        dp.put("documentCompletionRate", metrics.getDocumentCompletionRate());
        dp.put("goLiveReady", metrics.getGoLiveReady());
        section.setDataPoints(dp);

        return section;
    }

    private DeliveryReportSectionVO buildSection5EngineerWorklogs(Long projectId, AutoCollectedMetricsVO metrics) {
        DeliveryReportSectionVO section = newSection(5, "工程师工作记录");

        StringBuilder sb = new StringBuilder();
        if (metrics.getTotalWorklogs() == 0) {
            sb.append(NO_DATA).append("（暂无工程师工作日志记录）");
        } else {
            sb.append("工程师累计提交工作日志").append(metrics.getTotalWorklogs()).append("份，");
            sb.append("其中已提交/审批").append(metrics.getSubmittedWorklogs()).append("份，");
            sb.append("提交率：").append(metrics.getWorklogSubmissionRate()).append("%。\n");

            List<EngineerWorklog> worklogs = engineerWorklogMapper.selectList(
                    new LambdaQueryWrapper<EngineerWorklog>()
                            .eq(EngineerWorklog::getProjectId, projectId));
            long distinctDays = worklogs.stream()
                    .map(EngineerWorklog::getWorkDate)
                    .filter(Objects::nonNull)
                    .distinct().count();
            sb.append("覆盖工作天数：").append(distinctDays).append("天。");
        }

        section.setContent(sb.toString());

        List<String> highlights = new ArrayList<>();
        highlights.add("日志总数：" + metrics.getTotalWorklogs() + "份");
        highlights.add("提交率：" + metrics.getWorklogSubmissionRate() + "%");
        section.setHighlights(highlights);

        Map<String, Object> dp = new LinkedHashMap<>();
        dp.put("totalWorklogs", metrics.getTotalWorklogs());
        dp.put("submittedWorklogs", metrics.getSubmittedWorklogs());
        dp.put("worklogSubmissionRate", metrics.getWorklogSubmissionRate());
        section.setDataPoints(dp);

        return section;
    }

    private DeliveryReportSectionVO buildSection6ProcessQuality(Long projectId, Project project,
                                                                 AutoCollectedMetricsVO metrics) {
        DeliveryReportSectionVO section = newSection(6, "项目过程质量");

        StringBuilder sb = new StringBuilder();

        sb.append("问题工单统计：共").append(metrics.getTotalTickets()).append("个，");
        sb.append("已解决").append(metrics.getResolvedTickets()).append("个，");
        sb.append("解决率：").append(metrics.getTicketResolutionRate()).append("%。\n");

        List<SupportTicket> tickets = supportTicketMapper.selectList(
                new LambdaQueryWrapper<SupportTicket>()
                        .eq(SupportTicket::getProjectId, projectId));
        Map<String, Long> byLevel = tickets.stream()
                .filter(t -> t.getLevel() != null)
                .collect(Collectors.groupingBy(SupportTicket::getLevel, Collectors.counting()));
        if (!byLevel.isEmpty()) {
            sb.append("问题等级分布：");
            byLevel.forEach((level, count) -> sb.append(level).append("=").append(count).append("  "));
            sb.append("\n");
        }

        BigDecimal scheduleDeviation = calculateScheduleDeviation(project);
        if (scheduleDeviation != null) {
            sb.append("进度偏差：").append(scheduleDeviation).append("%");
            if (scheduleDeviation.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("（延期）");
            } else if (scheduleDeviation.compareTo(BigDecimal.ZERO) < 0) {
                sb.append("（提前）");
            } else {
                sb.append("（按期）");
            }
            sb.append("。");
        }

        section.setContent(sb.toString());

        List<String> highlights = new ArrayList<>();
        highlights.add("工单解决率：" + metrics.getTicketResolutionRate() + "%");
        if (scheduleDeviation != null) {
            highlights.add("进度偏差：" + scheduleDeviation + "%");
        }
        section.setHighlights(highlights);

        Map<String, Object> dp = new LinkedHashMap<>();
        dp.put("totalTickets", metrics.getTotalTickets());
        dp.put("resolvedTickets", metrics.getResolvedTickets());
        dp.put("ticketResolutionRate", metrics.getTicketResolutionRate());
        dp.put("scheduleDeviation", scheduleDeviation);
        dp.put("levelDistribution", byLevel);
        section.setDataPoints(dp);

        return section;
    }

    private DeliveryReportSectionVO buildSection7OutstandingIssues(Long projectId) {
        DeliveryReportSectionVO section = newSection(7, "遗留问题与建议");

        StringBuilder sb = new StringBuilder();
        List<String> highlights = new ArrayList<>();

        List<SupportTicket> openTickets = supportTicketMapper.selectList(
                new LambdaQueryWrapper<SupportTicket>()
                        .eq(SupportTicket::getProjectId, projectId)
                        .and(w -> w.eq(SupportTicket::getStatus, "OPEN")
                                .or().eq(SupportTicket::getStatus, "IN_PROGRESS")));

        if (!openTickets.isEmpty()) {
            sb.append("未解决工单（").append(openTickets.size()).append("个）：\n");
            for (SupportTicket ticket : openTickets) {
                sb.append("  - [").append(safe(ticket.getLevel())).append("] ")
                        .append(safe(ticket.getTitle())).append("\n");
                highlights.add("未解决：" + safe(ticket.getTitle()));
            }
        }

        List<SystemAlert> activeAlerts = systemAlertMapper.selectList(
                new LambdaQueryWrapper<SystemAlert>()
                        .eq(SystemAlert::getProjectId, projectId)
                        .and(w -> w.isNull(SystemAlert::getAcknowledged)
                                .or().eq(SystemAlert::getAcknowledged, false)));

        if (!activeAlerts.isEmpty()) {
            sb.append("\n活跃系统告警（").append(activeAlerts.size()).append("个）：\n");
            for (SystemAlert alert : activeAlerts) {
                sb.append("  - [").append(safe(alert.getSeverity())).append("] ")
                        .append(safe(alert.getTitle())).append("\n");
            }
        }

        List<ExamRecord> failedExams = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getProjectId, projectId)
                        .eq(ExamRecord::getPassed, false));
        Map<String, Long> weakPoints = failedExams.stream()
                .filter(e -> e.getWeakPoints() != null && !e.getWeakPoints().isBlank())
                .collect(Collectors.groupingBy(ExamRecord::getWeakPoints, Collectors.counting()));
        if (!weakPoints.isEmpty()) {
            sb.append("\n培训薄弱环节：\n");
            weakPoints.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> sb.append("  - ").append(e.getKey())
                            .append("（出现").append(e.getValue()).append("次）\n"));
        }

        if (sb.isEmpty()) {
            sb.append("无遗留问题。");
        }

        section.setContent(sb.toString());
        section.setHighlights(highlights.isEmpty() ? List.of("无遗留问题") : highlights);

        Map<String, Object> dp = new LinkedHashMap<>();
        dp.put("openTicketCount", openTickets.size());
        dp.put("activeAlertCount", activeAlerts.size());
        dp.put("unresolvedWeakPoints", weakPoints.size());
        section.setDataPoints(dp);

        return section;
    }

    private DeliveryReportSectionVO buildSection8MaintenancePlan(Project project) {
        DeliveryReportSectionVO section = newSection(8, "后续维护计划");

        String content = """
                1. 售后跟进期：上线后30天驻场/远程支持
                2. 定期回访：每月1次系统使用检查
                3. 版本升级：跟踪系统版本更新
                4. 知识库更新：持续完善操作手册和FAQ
                5. 培训补强：针对薄弱模块安排复训""";

        section.setContent(content);
        section.setHighlights(List.of(
                "30天售后跟进",
                "每月定期回访",
                "持续培训补强"
        ));

        Map<String, Object> dp = new LinkedHashMap<>();
        dp.put("supportPeriodDays", 30);
        dp.put("visitFrequency", "monthly");
        section.setDataPoints(dp);

        return section;
    }

    // === Helpers ===

    private BigDecimal calculateOverallScore(AutoCollectedMetricsVO m) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal weightSum = BigDecimal.ZERO;

        if (m.getTrainingPassRate() != null) {
            total = total.add(m.getTrainingPassRate().multiply(bd("0.20")));
            weightSum = weightSum.add(bd("0.20"));
        }
        if (m.getDataImportCompletionRate() != null) {
            total = total.add(m.getDataImportCompletionRate().multiply(bd("0.20")));
            weightSum = weightSum.add(bd("0.20"));
        }
        if (m.getTotalTickets() != null && m.getTotalTickets() > 0) {
            total = total.add(m.getTicketResolutionRate().multiply(bd("0.15")));
            weightSum = weightSum.add(bd("0.15"));
        }
        if (m.getDocumentCompletionRate() != null) {
            total = total.add(m.getDocumentCompletionRate().multiply(bd("0.15")));
            weightSum = weightSum.add(bd("0.15"));
        }
        if (m.getWorklogSubmissionRate() != null && m.getTotalWorklogs() != null && m.getTotalWorklogs() > 0) {
            total = total.add(m.getWorklogSubmissionRate().multiply(bd("0.15")));
            weightSum = weightSum.add(bd("0.15"));
        }
        if (m.getSimulationPassRate() != null && m.getSimulationPassRate().compareTo(BigDecimal.ZERO) > 0) {
            total = total.add(m.getSimulationPassRate().multiply(bd("0.15")));
            weightSum = weightSum.add(bd("0.15"));
        }

        if (weightSum.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return total.divide(weightSum, 2, RoundingMode.HALF_UP);
    }

    private String determineOverallStatus(BigDecimal score) {
        if (score.compareTo(bd("90")) >= 0) return "优秀";
        if (score.compareTo(bd("75")) >= 0) return "良好";
        if (score.compareTo(bd("60")) >= 0) return "合格";
        return "待改进";
    }

    private BigDecimal calculateScheduleDeviation(Project project) {
        if (project.getStartDate() == null || project.getEndDate() == null) {
            return null;
        }
        ProjectPlan plan = projectPlanMapper.selectOne(
                new LambdaQueryWrapper<ProjectPlan>()
                        .eq(ProjectPlan::getProjectId, project.getId())
                        .last("LIMIT 1"));
        if (plan == null || plan.getTotalDays() == null || plan.getTotalDays() <= 0) {
            return null;
        }
        long actualDays = ChronoUnit.DAYS.between(project.getStartDate(), LocalDate.now());
        long planned = plan.getTotalDays();
        return BigDecimal.valueOf((actualDays - planned) * 100.0 / planned)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String assessGoalStatus(ProjectStatus status) {
        if (status == null) return "未知";
        return switch (status) {
            case DELIVERED, AFTER_SALES -> "已达成";
            case GO_LIVE, FOLLOW_UP -> "部分达成";
            default -> "进行中";
        };
    }

    private void appendGoalRow(StringBuilder sb, List<String> highlights,
                               Map<String, Object> dp,
                               String goalName, String goalContent, String statusLabel) {
        if (goalContent != null && !goalContent.isBlank()) {
            sb.append(goalName).append("：").append(goalContent)
                    .append(" → ").append(statusLabel).append("\n");
            highlights.add(goalName + "：" + statusLabel);
            dp.put(goalName, Map.of("content", goalContent, "status", statusLabel));
        }
    }

    private DeliveryReportSectionVO newSection(int number, String title) {
        DeliveryReportSectionVO s = new DeliveryReportSectionVO();
        s.setSectionNumber(number);
        s.setSectionTitle(title);
        s.setHighlights(new ArrayList<>());
        s.setDataPoints(new LinkedHashMap<>());
        return s;
    }

    private static BigDecimal pct(int numerator, int denominator) {
        return BigDecimal.valueOf(numerator * 100.0 / denominator)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal bd(String val) {
        return new BigDecimal(val);
    }

    private static String safe(String val) {
        return val != null && !val.isBlank() ? val : NO_DATA;
    }
}
