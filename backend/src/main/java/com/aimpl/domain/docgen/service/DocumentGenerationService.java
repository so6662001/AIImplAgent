package com.aimpl.domain.docgen.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.docgen.vo.DocumentTypeVO;
import com.aimpl.domain.docgen.vo.GeneratedDocumentVO;
import com.aimpl.domain.project.dto.PlanGenerateRequestDTO;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.entity.ProjectPlan;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.project.mapper.ProjectPlanMapper;
import com.aimpl.domain.project.service.PlanGeneratorService;
import com.aimpl.domain.project.vo.GeneratedPlanVO;
import com.aimpl.domain.report.entity.DeliveryReport;
import com.aimpl.domain.report.mapper.DeliveryReportMapper;
import com.aimpl.domain.report.service.DeliveryReportGeneratorService;
import com.aimpl.domain.report.vo.GeneratedDeliveryReportVO;
import com.aimpl.domain.research.entity.CustomerProfile;
import com.aimpl.domain.research.entity.ResearchReport;
import com.aimpl.domain.research.mapper.CustomerProfileMapper;
import com.aimpl.domain.research.mapper.ResearchReportMapper;
import com.aimpl.domain.research.service.ReportGeneratorService;
import com.aimpl.domain.simulation.entity.SimulationReport;
import com.aimpl.domain.simulation.mapper.SimulationReportMapper;
import com.aimpl.domain.simulation.service.SimulationReportService;
import com.aimpl.domain.simulation.vo.SimulationReportVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentGenerationService {

    private final ReportGeneratorService researchReportGenerator;
    private final DeliveryReportGeneratorService deliveryReportGenerator;
    private final PlanGeneratorService planGenerator;
    private final SimulationReportService simulationReportService;
    private final ProjectMapper projectMapper;
    private final ProjectPlanMapper projectPlanMapper;
    private final CustomerProfileMapper customerProfileMapper;
    private final ResearchReportMapper researchReportMapper;
    private final DeliveryReportMapper deliveryReportMapper;
    private final SimulationReportMapper simulationReportMapper;

    public GeneratedDocumentVO generate(String documentType, Long projectId, Map<String, Object> params) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        return switch (documentType) {
            case "RESEARCH_REPORT" -> generateResearchReport(projectId);
            case "DELIVERY_PLAN" -> generateDeliveryPlan(projectId, params);
            case "DELIVERY_REPORT" -> generateDeliveryReport(projectId);
            case "SIMULATION_REPORT" -> generateSimulationReport(projectId);
            default -> throw new BizException("不支持的文档类型: " + documentType);
        };
    }

    public List<DocumentTypeVO> listDocumentTypes(Long projectId) {
        List<DocumentTypeVO> types = new ArrayList<>();

        types.add(buildDocumentType("RESEARCH_REPORT", "客户背景分析报告",
                "基于客户画像自动生成的企业背景、行业分析和风险评估报告",
                projectId));
        types.add(buildDocumentType("DELIVERY_PLAN", "项目交付计划",
                "包含WBS、里程碑、甘特图和资源规划的项目交付计划",
                projectId));
        types.add(buildDocumentType("SIMULATION_REPORT", "模拟演练评估报告",
                "模拟演练场景执行结果和上线准备度评估报告",
                projectId));
        types.add(buildDocumentType("DELIVERY_REPORT", "项目交付成果报告",
                "全面的项目交付成果汇总报告，包含培训、数据、质量等维度",
                projectId));

        return types;
    }

    private GeneratedDocumentVO generateResearchReport(Long projectId) {
        CustomerProfile profile = customerProfileMapper.selectOne(
                new LambdaQueryWrapper<CustomerProfile>()
                        .eq(CustomerProfile::getProjectId, projectId)
                        .last("LIMIT 1"));
        if (profile == null) {
            throw new BizException("该项目下没有客户画像，请先创建客户画像");
        }

        var reportVO = researchReportGenerator.generateReport(profile.getId());

        GeneratedDocumentVO doc = new GeneratedDocumentVO();
        doc.setDocumentType("RESEARCH_REPORT");
        doc.setDocumentName(reportVO.getCompanyName() + " - 客户背景分析报告");
        doc.setProjectId(projectId);
        doc.setGeneratedAt(reportVO.getGeneratedAt());
        doc.setStatus("GENERATED");

        StringBuilder content = new StringBuilder();
        List<String> sectionTitles = new ArrayList<>();
        if (reportVO.getSections() != null) {
            for (var section : reportVO.getSections()) {
                sectionTitles.add(section.getSectionTitle());
                content.append("## ").append(section.getSectionTitle()).append("\n\n");
                content.append(section.getContent()).append("\n\n");
            }
        }
        content.append("综合风险评级: ").append(reportVO.getOverallRiskLevel());
        doc.setContent(content.toString());
        doc.setSections(sectionTitles);
        return doc;
    }

    private GeneratedDocumentVO generateDeliveryPlan(Long projectId, Map<String, Object> params) {
        PlanGenerateRequestDTO request = new PlanGenerateRequestDTO();
        request.setProjectId(projectId);

        if (params != null) {
            if (params.containsKey("customizationLevel")) {
                request.setCustomizationLevel((String) params.get("customizationLevel"));
            }
            if (params.containsKey("deadline") && params.get("deadline") != null) {
                request.setDeadline(LocalDate.parse(params.get("deadline").toString()));
            }
            if (params.containsKey("budget")) {
                request.setBudget((String) params.get("budget"));
            }
            if (params.containsKey("integrationRequirements")) {
                @SuppressWarnings("unchecked")
                List<String> integrations = (List<String>) params.get("integrationRequirements");
                request.setIntegrationRequirements(integrations);
            }
        }

        GeneratedPlanVO planVO = planGenerator.generatePlan(request);

        GeneratedDocumentVO doc = new GeneratedDocumentVO();
        doc.setDocumentType("DELIVERY_PLAN");
        doc.setDocumentName("项目交付计划");
        doc.setProjectId(projectId);
        doc.setGeneratedAt(LocalDateTime.now());
        doc.setStatus("GENERATED");

        StringBuilder content = new StringBuilder();
        content.append("预计工期: ").append(planVO.getEstimatedDurationDays()).append("天\n");
        content.append("风险等级: ").append(planVO.getRiskLevel()).append("\n\n");

        List<String> sectionTitles = new ArrayList<>();
        sectionTitles.add("里程碑计划");
        content.append("## 里程碑计划\n");
        if (planVO.getMilestones() != null) {
            for (var ms : planVO.getMilestones()) {
                content.append("- Day ").append(ms.getDayOffset())
                        .append(": ").append(ms.getName())
                        .append(" (").append(ms.getDeliverables()).append(")\n");
            }
        }

        sectionTitles.add("WBS任务分解");
        content.append("\n## WBS任务分解\n");
        if (planVO.getWbsItems() != null) {
            content.append("共").append(planVO.getWbsItems().size()).append("项任务\n");
        }

        sectionTitles.add("资源规划");
        content.append("\n## 资源规划\n");
        if (planVO.getResourcePlan() != null) {
            for (var res : planVO.getResourcePlan()) {
                content.append("- ").append(res.getRole())
                        .append(" x").append(res.getHeadcount())
                        .append(" (").append(res.getPhase()).append(")\n");
            }
        }

        sectionTitles.add("风险管理");
        content.append("\n## 风险管理\n");
        if (planVO.getRiskPlan() != null) {
            for (var risk : planVO.getRiskPlan()) {
                content.append("- [").append(risk.getRiskLevel()).append("] ")
                        .append(risk.getRiskName()).append("\n");
            }
        }

        doc.setContent(content.toString());
        doc.setSections(sectionTitles);
        return doc;
    }

    private GeneratedDocumentVO generateDeliveryReport(Long projectId) {
        GeneratedDeliveryReportVO reportVO = deliveryReportGenerator.autoGenerate(projectId);

        GeneratedDocumentVO doc = new GeneratedDocumentVO();
        doc.setDocumentType("DELIVERY_REPORT");
        doc.setDocumentName(reportVO.getProjectCode() + " 交付报告");
        doc.setProjectId(projectId);
        doc.setGeneratedAt(reportVO.getGeneratedAt());
        doc.setStatus("GENERATED");

        StringBuilder content = new StringBuilder();
        List<String> sectionTitles = new ArrayList<>();
        if (reportVO.getSections() != null) {
            for (var section : reportVO.getSections()) {
                sectionTitles.add(section.getSectionTitle());
                content.append("## ").append(section.getSectionTitle()).append("\n\n");
                content.append(section.getContent()).append("\n\n");
            }
        }
        content.append("综合评分: ").append(reportVO.getOverallScore())
                .append(" (").append(reportVO.getOverallStatus()).append(")");
        doc.setContent(content.toString());
        doc.setSections(sectionTitles);
        return doc;
    }

    private GeneratedDocumentVO generateSimulationReport(Long projectId) {
        SimulationReportVO reportVO = simulationReportService.generateEvaluationReport(projectId);

        GeneratedDocumentVO doc = new GeneratedDocumentVO();
        doc.setDocumentType("SIMULATION_REPORT");
        doc.setDocumentName("模拟演练评估报告");
        doc.setProjectId(projectId);
        doc.setGeneratedAt(reportVO.getGeneratedAt());
        doc.setStatus("GENERATED");

        StringBuilder content = new StringBuilder();
        content.append("场景总数: ").append(reportVO.getTotalScenes()).append("\n");
        content.append("已执行: ").append(reportVO.getExecutedScenes()).append("\n");
        content.append("通过: ").append(reportVO.getPassedScenes()).append("\n");
        content.append("失败: ").append(reportVO.getFailedScenes()).append("\n");
        content.append("待执行: ").append(reportVO.getPendingScenes()).append("\n");
        content.append("整体通过率: ").append(reportVO.getOverallPassRate()).append("%\n");
        content.append("整体评分: ").append(reportVO.getOverallScore()).append("\n");
        content.append("可进入培训阶段: ").append(reportVO.isReadyForTraining() ? "是" : "否").append("\n\n");

        List<String> sectionTitles = new ArrayList<>();
        sectionTitles.add("场景执行结果");
        content.append("## 场景执行结果\n");
        if (reportVO.getSceneResults() != null) {
            for (var scene : reportVO.getSceneResults()) {
                content.append("- ").append(scene.getSceneName())
                        .append(" [").append(scene.getStatus()).append("]");
                if (scene.getScore() != null) {
                    content.append(" 评分:").append(scene.getScore());
                }
                content.append("\n");
            }
        }

        sectionTitles.add("建议");
        content.append("\n## 建议\n");
        if (reportVO.getRecommendations() != null) {
            for (var rec : reportVO.getRecommendations()) {
                content.append("- ").append(rec).append("\n");
            }
        }

        doc.setContent(content.toString());
        doc.setSections(sectionTitles);
        return doc;
    }

    private DocumentTypeVO buildDocumentType(String typeCode, String typeName,
                                              String description, Long projectId) {
        DocumentTypeVO vo = new DocumentTypeVO();
        vo.setTypeCode(typeCode);
        vo.setTypeName(typeName);
        vo.setDescription(description);

        switch (typeCode) {
            case "RESEARCH_REPORT" -> {
                List<ResearchReport> reports = researchReportMapper.selectList(
                        new LambdaQueryWrapper<ResearchReport>()
                                .eq(ResearchReport::getProjectId, projectId)
                                .orderByDesc(ResearchReport::getCreateTime));
                vo.setRecordCount(reports.size());
                vo.setGenerated(!reports.isEmpty());
                if (!reports.isEmpty()) {
                    vo.setLastGeneratedAt(reports.get(0).getCreateTime());
                }
            }
            case "DELIVERY_PLAN" -> {
                List<ProjectPlan> plans = projectPlanMapper.selectList(
                        new LambdaQueryWrapper<ProjectPlan>()
                                .eq(ProjectPlan::getProjectId, projectId)
                                .orderByDesc(ProjectPlan::getCreateTime));
                vo.setRecordCount(plans.size());
                vo.setGenerated(!plans.isEmpty());
                if (!plans.isEmpty()) {
                    vo.setLastGeneratedAt(plans.get(0).getCreateTime());
                }
            }
            case "DELIVERY_REPORT" -> {
                List<DeliveryReport> reports = deliveryReportMapper.selectList(
                        new LambdaQueryWrapper<DeliveryReport>()
                                .eq(DeliveryReport::getProjectId, projectId)
                                .orderByDesc(DeliveryReport::getCreateTime));
                vo.setRecordCount(reports.size());
                vo.setGenerated(!reports.isEmpty());
                if (!reports.isEmpty()) {
                    vo.setLastGeneratedAt(reports.get(0).getCreateTime());
                }
            }
            case "SIMULATION_REPORT" -> {
                List<SimulationReport> reports = simulationReportMapper.selectList(
                        new LambdaQueryWrapper<SimulationReport>()
                                .eq(SimulationReport::getProjectId, projectId)
                                .orderByDesc(SimulationReport::getCreateTime));
                vo.setRecordCount(reports.size());
                vo.setGenerated(!reports.isEmpty());
                if (!reports.isEmpty()) {
                    vo.setLastGeneratedAt(reports.get(0).getCreateTime());
                }
            }
        }

        return vo;
    }
}
