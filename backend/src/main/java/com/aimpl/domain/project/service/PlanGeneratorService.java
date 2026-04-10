package com.aimpl.domain.project.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.dto.PlanGenerateRequestDTO;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.project.vo.GeneratedPlanVO;
import com.aimpl.domain.project.vo.GeneratedPlanVO.*;
import com.aimpl.domain.research.entity.CustomerProfile;
import com.aimpl.domain.research.mapper.CustomerProfileMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PlanGeneratorService {

    private final ProjectMapper projectMapper;
    private final CustomerProfileMapper customerProfileMapper;

    public GeneratedPlanVO generatePlan(PlanGenerateRequestDTO request) {
        Project project = projectMapper.selectById(request.getProjectId());
        if (project == null) {
            throw new BizException("项目不存在: " + request.getProjectId());
        }

        CustomerProfile profile = customerProfileMapper.selectOne(
                new LambdaQueryWrapper<CustomerProfile>()
                        .eq(CustomerProfile::getProjectId, request.getProjectId())
                        .last("LIMIT 1")
        );

        String scale = project.getScale() != null ? project.getScale() : "中型";
        List<String> modules = project.getModules() != null
                ? Arrays.asList(project.getModules().split(","))
                : Collections.emptyList();
        String customizationLevel = request.getCustomizationLevel() != null
                ? request.getCustomizationLevel() : "标准";
        List<String> integrations = request.getIntegrationRequirements() != null
                ? request.getIntegrationRequirements() : Collections.emptyList();

        boolean hasMes = modules.stream().anyMatch(m -> "MES".equalsIgnoreCase(m.trim()));
        boolean isDeepCustom = "深度定制".equals(customizationLevel);
        boolean isLightCustom = "轻度定制".equals(customizationLevel);
        int staffCount = (profile != null && profile.getTotalStaff() != null) ? profile.getTotalStaff() : 0;
        int productionLines = (profile != null && profile.getTotalProductionLines() != null) ? profile.getTotalProductionLines() : 0;

        int duration = calculateDuration(scale, modules, customizationLevel, integrations, staffCount, productionLines);

        List<MilestoneItem> milestones = generateMilestones(duration, hasMes, isDeepCustom);

        List<WbsItem> wbsItems = generateWbsItems(milestones);

        List<ResourceItem> resourcePlan = generateResourcePlan(scale, hasMes);

        List<RiskItem> riskPlan = generateRiskPlan(
                isDeepCustom, !integrations.isEmpty(), hasMes, staffCount,
                request.getDeadline(), duration
        );

        List<GanttItem> ganttData = generateGanttData(wbsItems);

        String overallRisk = determineOverallRisk(riskPlan);

        GeneratedPlanVO vo = new GeneratedPlanVO();
        vo.setEstimatedDurationDays(duration);
        vo.setRiskLevel(overallRisk);
        vo.setMilestones(milestones);
        vo.setWbsItems(wbsItems);
        vo.setResourcePlan(resourcePlan);
        vo.setRiskPlan(riskPlan);
        vo.setGanttData(ganttData);
        return vo;
    }

    int calculateDuration(String scale, List<String> modules, String customizationLevel,
                          List<String> integrations, int staffCount, int productionLines) {
        int base;
        switch (scale) {
            case "大型": base = 90; break;
            case "小型": base = 30; break;
            default: base = 60; break;
        }

        int moduleCount = modules.size();
        if (moduleCount > 3) {
            base += (moduleCount - 3) * 15;
        }

        if (modules.stream().anyMatch(m -> "MES".equalsIgnoreCase(m.trim()))) {
            base += 20;
        }

        if ("深度定制".equals(customizationLevel)) {
            base += 30;
        } else if ("轻度定制".equals(customizationLevel)) {
            base += 15;
        }

        base += integrations.size() * 10;

        if (staffCount > 100) {
            base += 10;
        }

        if (productionLines > 0) {
            base += 15;
        }

        return base;
    }

    List<MilestoneItem> generateMilestones(int duration, boolean hasMes, boolean isDeepCustom) {
        List<MilestoneItem> milestones = new ArrayList<>();
        milestones.add(new MilestoneItem("调研完成", pct(duration, 0.08), "客户画像+背景分析报告"));
        milestones.add(new MilestoneItem("计划确认", pct(duration, 0.13), "交付计划+WBS"));
        milestones.add(new MilestoneItem("模拟演练通过", pct(duration, 0.28), "模拟演练评估报告"));

        if (hasMes) {
            milestones.add(new MilestoneItem("MES配置完成", pct(duration, 0.40), "MES系统配置与对接完成"));
        }

        milestones.add(new MilestoneItem("培训完成", pct(duration, 0.50), "全员考核通过"));

        if (isDeepCustom) {
            milestones.add(new MilestoneItem("定制开发完成", pct(duration, 0.60), "定制功能开发与测试完成"));
        }

        milestones.add(new MilestoneItem("期初数据完成", pct(duration, 0.65), "期初数据收集与清洗完成"));
        milestones.add(new MilestoneItem("数据导入完成", pct(duration, 0.75), "数据导入与验证完成"));
        milestones.add(new MilestoneItem("正式上线", pct(duration, 0.88), "系统正式启用"));
        milestones.add(new MilestoneItem("跟进辅助完成", pct(duration, 0.95), "现场辅助与优化完成"));
        milestones.add(new MilestoneItem("交付验收", duration, "交付成果报告+客户签字"));

        return milestones;
    }

    List<WbsItem> generateWbsItems(List<MilestoneItem> milestones) {
        Map<String, Integer> msMap = new LinkedHashMap<>();
        for (MilestoneItem m : milestones) {
            msMap.put(m.getName(), m.getDayOffset());
        }

        int researchEnd = msMap.getOrDefault("调研完成", 0);
        int planEnd = msMap.getOrDefault("计划确认", 0);
        int drillEnd = msMap.getOrDefault("模拟演练通过", 0);
        int trainEnd = msMap.getOrDefault("培训完成", 0);
        int dataCollectEnd = msMap.getOrDefault("期初数据完成", 0);
        int dataImportEnd = msMap.getOrDefault("数据导入完成", 0);
        int goLiveEnd = msMap.getOrDefault("正式上线", 0);
        int followEnd = msMap.getOrDefault("跟进辅助完成", 0);
        int acceptEnd = msMap.getOrDefault("交付验收", 0);

        List<WbsItem> items = new ArrayList<>();

        // Phase 1 - 调研
        int p1Start = 0;
        int p1Len = researchEnd - p1Start;
        items.add(new WbsItem("调研", "项目启动会", p1Start, p1Start + seg(p1Len, 0.2), "PM", "启动会纪要"));
        items.add(new WbsItem("调研", "客户调研访谈", p1Start + seg(p1Len, 0.2), p1Start + seg(p1Len, 0.5), "PM", "调研访谈记录"));
        items.add(new WbsItem("调研", "业务流程梳理", p1Start + seg(p1Len, 0.5), p1Start + seg(p1Len, 0.8), "实施工程师", "流程图"));
        items.add(new WbsItem("调研", "调研报告编写", p1Start + seg(p1Len, 0.8), researchEnd, "PM", "调研报告"));

        // Phase 2 - 计划
        int p2Start = researchEnd;
        int p2Len = planEnd - p2Start;
        items.add(new WbsItem("计划", "交付计划制定", p2Start, p2Start + seg(p2Len, 0.4), "PM", "交付计划文档"));
        items.add(new WbsItem("计划", "资源协调", p2Start + seg(p2Len, 0.4), p2Start + seg(p2Len, 0.7), "PM", "资源分配表"));
        items.add(new WbsItem("计划", "计划评审", p2Start + seg(p2Len, 0.7), planEnd, "PM", "评审纪要"));

        // Phase 3 - 演练
        int p3Start = planEnd;
        int p3Len = drillEnd - p3Start;
        items.add(new WbsItem("演练", "环境搭建", p3Start, p3Start + seg(p3Len, 0.25), "实施工程师", "测试环境"));
        items.add(new WbsItem("演练", "模拟数据准备", p3Start + seg(p3Len, 0.25), p3Start + seg(p3Len, 0.5), "实施工程师", "模拟数据"));
        items.add(new WbsItem("演练", "模拟演练执行", p3Start + seg(p3Len, 0.5), p3Start + seg(p3Len, 0.8), "实施工程师", "演练记录"));
        items.add(new WbsItem("演练", "演练评估", p3Start + seg(p3Len, 0.8), drillEnd, "PM", "演练评估报告"));

        // Phase 4 - 培训
        int p4Start = drillEnd;
        int p4Len = trainEnd - p4Start;
        items.add(new WbsItem("培训", "培训材料准备", p4Start, p4Start + seg(p4Len, 0.15), "实施工程师", "培训教材"));
        items.add(new WbsItem("培训", "管理层培训", p4Start + seg(p4Len, 0.15), p4Start + seg(p4Len, 0.35), "PM", "管理层培训记录"));
        items.add(new WbsItem("培训", "业务骨干培训", p4Start + seg(p4Len, 0.35), p4Start + seg(p4Len, 0.6), "实施工程师", "骨干培训记录"));
        items.add(new WbsItem("培训", "操作人员培训", p4Start + seg(p4Len, 0.6), p4Start + seg(p4Len, 0.85), "实施工程师", "操作培训记录"));
        items.add(new WbsItem("培训", "考核", p4Start + seg(p4Len, 0.85), trainEnd, "PM", "考核结果"));

        // Phase 5 - 数据
        int p5Start = trainEnd;
        int p5Mid = dataCollectEnd;
        int p5End = dataImportEnd;
        int p5Len = p5End - p5Start;
        items.add(new WbsItem("数据", "数据收集模板下发", p5Start, p5Start + seg(p5Len, 0.15), "实施工程师", "数据模板"));
        items.add(new WbsItem("数据", "数据清洗", p5Start + seg(p5Len, 0.15), p5Start + seg(p5Len, 0.4), "客户方", "清洗后数据"));
        items.add(new WbsItem("数据", "数据校验", p5Start + seg(p5Len, 0.4), p5Start + seg(p5Len, 0.6), "实施工程师", "校验报告"));
        items.add(new WbsItem("数据", "数据导入", p5Start + seg(p5Len, 0.6), p5Start + seg(p5Len, 0.85), "实施工程师", "导入记录"));
        items.add(new WbsItem("数据", "导入验证", p5Start + seg(p5Len, 0.85), p5End, "PM", "验证报告"));

        // Phase 6 - 上线
        int p6Start = dataImportEnd;
        int p6Len = goLiveEnd - p6Start;
        items.add(new WbsItem("上线", "上线检查", p6Start, p6Start + seg(p6Len, 0.3), "PM", "上线检查清单"));
        items.add(new WbsItem("上线", "帐套启用", p6Start + seg(p6Len, 0.3), p6Start + seg(p6Len, 0.6), "实施工程师", "帐套启用记录"));
        items.add(new WbsItem("上线", "并行运行", p6Start + seg(p6Len, 0.6), goLiveEnd, "客户方", "并行运行报告"));

        // Phase 7 - 跟进
        int p7Start = goLiveEnd;
        int p7Len = followEnd - p7Start;
        items.add(new WbsItem("跟进", "现场辅助", p7Start, p7Start + seg(p7Len, 0.4), "实施工程师", "辅助记录"));
        items.add(new WbsItem("跟进", "问题处理", p7Start + seg(p7Len, 0.4), p7Start + seg(p7Len, 0.75), "实施工程师", "问题处理记录"));
        items.add(new WbsItem("跟进", "优化调整", p7Start + seg(p7Len, 0.75), followEnd, "实施工程师", "优化报告"));

        // Phase 8 - 验收
        int p8Start = followEnd;
        int p8Len = acceptEnd - p8Start;
        items.add(new WbsItem("验收", "成果整理", p8Start, p8Start + seg(p8Len, 0.35), "PM", "成果清单"));
        items.add(new WbsItem("验收", "交付报告", p8Start + seg(p8Len, 0.35), p8Start + seg(p8Len, 0.7), "PM", "交付报告"));
        items.add(new WbsItem("验收", "客户验收签字", p8Start + seg(p8Len, 0.7), acceptEnd, "客户方", "验收签字单"));

        return items;
    }

    List<ResourceItem> generateResourcePlan(String scale, boolean hasMes) {
        List<ResourceItem> resources = new ArrayList<>();

        resources.add(new ResourceItem("PM", 1, "全程", "项目管理,沟通协调,风险控制"));

        switch (scale) {
            case "大型":
                resources.add(new ResourceItem("高级实施顾问", 2, "全程", "方案设计,流程优化,培训指导"));
                resources.add(new ResourceItem("实施工程师", 3, "演练-验收", "环境搭建,数据处理,系统配置"));
                break;
            case "小型":
                resources.add(new ResourceItem("实施工程师", 1, "全程", "环境搭建,数据处理,系统配置,培训"));
                break;
            default:
                resources.add(new ResourceItem("高级实施顾问", 1, "全程", "方案设计,流程优化,培训指导"));
                resources.add(new ResourceItem("实施工程师", 2, "演练-验收", "环境搭建,数据处理,系统配置"));
                break;
        }

        if (hasMes) {
            resources.add(new ResourceItem("MES顾问", 1, "演练-上线", "MES配置,数据同步,接口对接"));
        }

        resources.add(new ResourceItem("客户方关键用户", 0, "培训-验收", "业务配合,数据准备,验收确认"));

        return resources;
    }

    List<RiskItem> generateRiskPlan(boolean isDeepCustom, boolean hasIntegrations,
                                    boolean hasMes, int staffCount,
                                    LocalDate deadline, int duration) {
        List<RiskItem> risks = new ArrayList<>();

        risks.add(new RiskItem("客户方数据准备延迟", "MEDIUM",
                "影响数据导入进度，可能导致上线延期",
                "提前2周下发模板+专人跟进", false));

        risks.add(new RiskItem("关键用户培训效果不达标", "MEDIUM",
                "影响系统使用效率和用户满意度",
                "增加实操练习+补学机制", false));

        risks.add(new RiskItem("系统上线初期操作不熟练", "LOW",
                "影响初期业务处理效率",
                "驻场跟进+AI问答辅助", false));

        if (isDeepCustom) {
            risks.add(new RiskItem("定制开发延期", "HIGH",
                    "定制需求变更频繁，开发周期不可控",
                    "预留缓冲+分阶段交付", true));
        }

        if (hasIntegrations) {
            risks.add(new RiskItem("接口对接复杂度高", "HIGH",
                    "第三方系统接口不稳定或文档不完善",
                    "提前技术调研+联调测试", true));
        }

        if (hasMes) {
            risks.add(new RiskItem("MES与ERP数据同步风险", "MEDIUM",
                    "数据同步延迟或不一致影响生产管理",
                    "数据同步方案设计+压力测试", false));
        }

        if (staffCount > 100) {
            risks.add(new RiskItem("大规模培训组织难度", "MEDIUM",
                    "人员多、部门杂，培训协调困难",
                    "分批培训+关键用户先行", false));
        }

        if (deadline != null) {
            long daysUntilDeadline = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
            if (duration > daysUntilDeadline) {
                risks.add(new RiskItem("工期紧张", "HIGH",
                        "预估工期" + duration + "天，超出截止日期" + (duration - daysUntilDeadline) + "天",
                        "增加资源+并行推进", true));
            }
        }

        return risks;
    }

    List<GanttItem> generateGanttData(List<WbsItem> wbsItems) {
        List<GanttItem> gantt = new ArrayList<>();
        String prevPhase = null;

        for (WbsItem wbs : wbsItems) {
            String dep = null;
            if (prevPhase != null && !prevPhase.equals(wbs.getPhase())) {
                dep = prevPhase;
            }
            gantt.add(new GanttItem(wbs.getTaskName(), wbs.getPhase(),
                    wbs.getStartDay(), wbs.getEndDay(), 0, dep));
            prevPhase = wbs.getPhase();
        }

        return gantt;
    }

    String determineOverallRisk(List<RiskItem> risks) {
        boolean hasHigh = risks.stream().anyMatch(r -> "HIGH".equals(r.getRiskLevel()));
        boolean hasMedium = risks.stream().anyMatch(r -> "MEDIUM".equals(r.getRiskLevel()));
        if (hasHigh) return "HIGH";
        if (hasMedium) return "MEDIUM";
        return "LOW";
    }

    private static int pct(int total, double percent) {
        return (int) Math.round(total * percent);
    }

    private static int seg(int length, double percent) {
        return Math.max(1, (int) Math.round(length * percent));
    }
}
