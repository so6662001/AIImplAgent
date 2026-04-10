package com.aimpl.domain.research.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.research.dto.ReportSectionVO;
import com.aimpl.domain.research.dto.ResearchReportVO;
import com.aimpl.domain.research.entity.CustomerProfile;
import com.aimpl.domain.research.entity.ResearchReport;
import com.aimpl.domain.research.mapper.ResearchReportMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportGeneratorService extends ServiceImpl<ResearchReportMapper, ResearchReport> {

    private final CustomerProfileService customerProfileService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ResearchReportVO generateReport(Long profileId) {
        CustomerProfile profile = customerProfileService.getById(profileId);
        if (profile == null) {
            throw new BizException("客户画像不存在: " + profileId);
        }

        List<ReportSectionVO> sections = new ArrayList<>();
        sections.add(buildCompanyOverview(profile));
        sections.add(buildBusinessModelAnalysis(profile));
        sections.add(buildBusinessProcessAnalysis(profile));
        sections.add(buildGoalSummary(profile));
        sections.add(buildRiskAssessment(profile));
        sections.add(buildDeliverableDefinition(profile));

        String riskLevel = assessOverallRisk(profile);

        ResearchReportVO vo = ResearchReportVO.builder()
                .profileId(profileId)
                .companyName(profile.getCompanyName())
                .generatedAt(LocalDateTime.now())
                .sections(sections)
                .overallRiskLevel(riskLevel)
                .build();

        ResearchReport entity = new ResearchReport();
        entity.setProjectId(profile.getProjectId());
        entity.setProfileId(profileId);
        entity.setOverallRiskLevel(riskLevel);
        entity.setStatus("DRAFT");
        try {
            entity.setReportContent(objectMapper.writeValueAsString(vo));
        } catch (JsonProcessingException e) {
            throw new BizException("报告序列化失败");
        }
        save(entity);

        return vo;
    }

    public List<ResearchReport> listByProjectId(Long projectId) {
        return list(new LambdaQueryWrapper<ResearchReport>()
                .eq(ResearchReport::getProjectId, projectId)
                .orderByDesc(ResearchReport::getCreateTime));
    }

    private ReportSectionVO buildCompanyOverview(CustomerProfile p) {
        StringBuilder sb = new StringBuilder();
        sb.append(p.getCompanyName());

        String typeLabel = p.getIndustryType() != null ? p.getIndustryType().getLabel() : "未知类型";
        sb.append("是一家").append(typeLabel).append("企业");

        if (hasText(p.getLegalPerson())) {
            sb.append("，法定代表人为").append(p.getLegalPerson());
        }
        if (hasText(p.getRegisteredCapital())) {
            sb.append("，注册资本").append(p.getRegisteredCapital());
        }
        if (p.getEstablishmentDate() != null) {
            sb.append("，成立于").append(p.getEstablishmentDate());
        }
        if (hasText(p.getAddress())) {
            sb.append("，办公地址位于").append(p.getAddress());
        }
        sb.append("。");

        if (hasText(p.getMainBusiness())) {
            sb.append("主营业务：").append(p.getMainBusiness()).append("。");
        }
        if (p.getTotalStaff() != null && p.getTotalStaff() > 0) {
            sb.append("现有员工").append(p.getTotalStaff()).append("人。");
        }

        List<String> highlights = new ArrayList<>();
        highlights.add("行业定位：" + typeLabel);
        if (p.getTotalStaff() != null && p.getTotalStaff() > 0) {
            highlights.add("企业规模：" + p.getTotalStaff() + "人");
        }
        if (hasText(p.getMainBusiness())) {
            highlights.add("主营业务：" + p.getMainBusiness());
        }

        return ReportSectionVO.builder()
                .sectionTitle("企业概况与行业定位")
                .content(sb.toString())
                .highlights(highlights)
                .build();
    }

    private ReportSectionVO buildBusinessModelAnalysis(CustomerProfile p) {
        StringBuilder sb = new StringBuilder();

        if (p.getBusinessModel() != null) {
            sb.append("经营模式：").append(p.getBusinessModel().getLabel()).append("。");
        }
        if (p.getTradeMode() != null) {
            sb.append("贸易模式：").append(p.getTradeMode().getLabel()).append("。");
        }
        if (p.getTradeScope() != null) {
            sb.append("贸易范围：").append(p.getTradeScope().getLabel()).append("。");
        }
        if (hasText(p.getSalesMode())) {
            sb.append("销售模式：").append(p.getSalesMode()).append("。");
        }
        if (hasText(p.getPricingModel())) {
            sb.append("定价模式：").append(p.getPricingModel()).append("。");
        }
        if (hasText(p.getSettlementMethods())) {
            sb.append("结算方式：").append(p.getSettlementMethods()).append("。");
        }
        if (hasText(p.getCreditPolicy())) {
            sb.append("信用政策：").append(p.getCreditPolicy()).append("。");
        }
        if (p.getMonthlyVolume() != null) {
            sb.append("月均销量：").append(p.getMonthlyVolume()).append("吨。");
        }
        if (p.getMonthlyAmount() != null) {
            sb.append("月均销售额：").append(p.getMonthlyAmount()).append("万元。");
        }

        if (sb.isEmpty()) {
            sb.append("暂无经营模式相关数据，建议补充完善。");
        }

        List<String> highlights = new ArrayList<>();
        if (p.getBusinessModel() != null) highlights.add("经营模式：" + p.getBusinessModel().getLabel());
        if (hasText(p.getSalesMode())) highlights.add("销售模式：" + p.getSalesMode());
        if (hasText(p.getPricingModel())) highlights.add("定价模式：" + p.getPricingModel());

        return ReportSectionVO.builder()
                .sectionTitle("经营模式分析")
                .content(sb.toString())
                .highlights(highlights)
                .build();
    }

    private ReportSectionVO buildBusinessProcessAnalysis(CustomerProfile p) {
        StringBuilder sb = new StringBuilder();
        List<String> highlights = new ArrayList<>();

        String type = p.getIndustryType() != null ? p.getIndustryType().name() : "";

        switch (type) {
            case "STEEL_MILL" -> {
                sb.append("作为钢厂企业，核心业务流程涵盖：原材料采购→生产排产→生产执行→质量检验→入库→销售发货。");
                if (p.getTotalProductionLines() != null && p.getTotalProductionLines() > 0) {
                    sb.append("当前拥有").append(p.getTotalProductionLines()).append("条产线");
                    if (hasText(p.getProductionShifts())) {
                        sb.append("，实行").append(p.getProductionShifts()).append("制");
                    }
                    sb.append("。");
                }
                if (hasText(p.getMesCurrentStatus())) {
                    sb.append("MES系统现状：").append(p.getMesCurrentStatus()).append("。");
                }
                if (hasText(p.getQualityStandards())) {
                    sb.append("质量标准：").append(p.getQualityStandards()).append("。");
                }
                highlights.add("核心流程：采购→生产→质检→销售");
                if (p.getTotalProductionLines() != null) highlights.add("产线数量：" + p.getTotalProductionLines());
            }
            case "STEEL_TRADER" -> {
                sb.append("作为钢贸商企业，核心业务流程涵盖：采购下单→到货入库→库存管理→销售出库→物流配送→结算对账。");
                if (p.getTotalWarehouseCount() != null && p.getTotalWarehouseCount() > 0) {
                    sb.append("当前拥有").append(p.getTotalWarehouseCount()).append("个仓库");
                    if (p.getTotalWarehouseAreaSqm() != null) {
                        sb.append("，总面积").append(p.getTotalWarehouseAreaSqm()).append("㎡");
                    }
                    sb.append("。");
                }
                if (p.getTotalCraneCount() != null && p.getTotalCraneCount() > 0) {
                    sb.append("配备行车").append(p.getTotalCraneCount()).append("台。");
                }
                highlights.add("核心流程：采购→入库→销售→物流→结算");
                if (p.getTotalWarehouseCount() != null) highlights.add("仓库数量：" + p.getTotalWarehouseCount());
            }
            case "PROCESSING_CENTER" -> {
                sb.append("作为加工中心企业，核心业务流程涵盖：来料接收→加工排产→加工执行→质检→成品入库→交付。");
                if (p.getTotalProductionLines() != null && p.getTotalProductionLines() > 0) {
                    sb.append("当前拥有").append(p.getTotalProductionLines()).append("条加工线。");
                }
                highlights.add("核心流程：来料→加工→质检→交付");
            }
            default -> {
                sb.append("作为综合服务商企业，核心业务流程涵盖：采购管理→库存管理→销售管理→加工服务→物流配送→财务结算。");
                highlights.add("核心流程：采购→库存→销售→加工→物流→结算");
            }
        }

        if (hasText(p.getTargetModules())) {
            sb.append("目标上线模块：").append(p.getTargetModules()).append("，");
            sb.append("系统需覆盖上述核心业务流程。");
        }

        return ReportSectionVO.builder()
                .sectionTitle("业务流程梳理")
                .content(sb.toString())
                .highlights(highlights)
                .build();
    }

    private ReportSectionVO buildGoalSummary(CustomerProfile p) {
        StringBuilder sb = new StringBuilder();
        List<String> highlights = new ArrayList<>();

        if (hasText(p.getManagementGoals())) {
            sb.append("管理目标：").append(p.getManagementGoals()).append("\n");
            highlights.add("管理目标已明确");
        }
        if (hasText(p.getProcessGoals())) {
            sb.append("流程优化目标：").append(p.getProcessGoals()).append("\n");
            highlights.add("流程优化目标已明确");
        }
        if (hasText(p.getEfficiencyGoals())) {
            sb.append("效率提升目标：").append(p.getEfficiencyGoals()).append("\n");
            highlights.add("效率提升目标已明确");
        }
        if (hasText(p.getRiskControlGoals())) {
            sb.append("风控目标：").append(p.getRiskControlGoals()).append("\n");
            highlights.add("风控目标已明确");
        }

        if (sb.isEmpty()) {
            sb.append("项目目标尚未明确，建议在调研阶段进一步与客户沟通确认。");
            highlights.add("建议：尽快明确项目目标");
        }

        if (hasText(p.getTargetModules())) {
            sb.append("\n优先级建议：");
            if (hasText(p.getModulePriorities())) {
                sb.append(p.getModulePriorities());
            } else {
                sb.append("建议优先上线核心业务模块（进销存），再推进财务及辅助模块。");
            }
        }

        return ReportSectionVO.builder()
                .sectionTitle("项目目标总结与优先级建议")
                .content(sb.toString())
                .highlights(highlights)
                .build();
    }

    private ReportSectionVO buildRiskAssessment(CustomerProfile p) {
        StringBuilder sb = new StringBuilder();
        List<String> highlights = new ArrayList<>();
        int riskScore = 0;

        if (p.getTotalStaff() != null && p.getTotalStaff() > 500) {
            sb.append("【组织规模风险】员工").append(p.getTotalStaff()).append("人，属大型企业，")
                    .append("培训覆盖和系统推广难度较高。\n");
            highlights.add("大型企业培训推广风险");
            riskScore += 2;
        } else if (p.getTotalStaff() != null && p.getTotalStaff() > 200) {
            sb.append("【组织规模】员工").append(p.getTotalStaff()).append("人，属中型企业，")
                    .append("需要制定分阶段培训计划。\n");
            riskScore += 1;
        }

        if (hasText(p.getExistingSystems())) {
            sb.append("【系统迁移风险】现有系统：").append(p.getExistingSystems())
                    .append("，需评估数据迁移方案和切换策略。\n");
            highlights.add("存在系统迁移需求");
            riskScore += 2;
        }

        if (p.getTotalProductionLines() != null && p.getTotalProductionLines() > 3) {
            sb.append("【生产复杂度风险】").append(p.getTotalProductionLines())
                    .append("条产线，MES集成复杂度较高。\n");
            highlights.add("多产线MES集成复杂");
            riskScore += 2;
        }

        if (p.getTotalWarehouseCount() != null && p.getTotalWarehouseCount() > 5) {
            sb.append("【多仓管理风险】").append(p.getTotalWarehouseCount())
                    .append("个仓库，多仓协同管理需重点关注。\n");
            highlights.add("多仓协同管理风险");
            riskScore += 1;
        }

        if (hasText(p.getDecisionChain()) && p.getDecisionChain().length() > 20) {
            sb.append("【决策链风险】决策链较长（").append(p.getDecisionChain())
                    .append("），项目推进可能受审批流程影响。\n");
            riskScore += 1;
        }

        if (sb.isEmpty()) {
            sb.append("基于当前已知信息，项目整体风险可控。建议在调研阶段进一步识别潜在风险。");
            highlights.add("整体风险可控");
        }

        String level = riskScore >= 5 ? "HIGH" : riskScore >= 3 ? "MEDIUM" : "LOW";
        sb.append("\n综合风险评级：").append(level);
        highlights.add("风险评级：" + level);

        return ReportSectionVO.builder()
                .sectionTitle("交付风险预评估")
                .content(sb.toString())
                .highlights(highlights)
                .build();
    }

    private ReportSectionVO buildDeliverableDefinition(CustomerProfile p) {
        StringBuilder sb = new StringBuilder();
        List<String> highlights = new ArrayList<>();

        sb.append("基于调研分析，建议项目交付成果包括：\n");

        if (hasText(p.getTargetModules())) {
            String[] modules = p.getTargetModules().split("[,，]");
            for (int i = 0; i < modules.length; i++) {
                String module = modules[i].trim();
                if (!module.isEmpty()) {
                    sb.append(i + 1).append(". ").append(module).append("模块上线运行\n");
                    highlights.add(module + "模块");
                }
            }
        } else {
            sb.append("1. 核心业务系统上线运行\n");
            highlights.add("核心业务系统");
        }

        sb.append("\n配套交付物：\n");
        sb.append("- 系统操作手册\n");
        sb.append("- 业务流程规范文档\n");
        sb.append("- 数据迁移报告\n");
        sb.append("- 用户培训记录\n");
        sb.append("- 上线验收报告\n");

        if (p.getTotalStaff() != null && p.getTotalStaff() > 0) {
            sb.append("\n培训覆盖目标：关键用户100%通过考核，普通用户操作培训覆盖率≥90%。");
        }

        return ReportSectionVO.builder()
                .sectionTitle("项目成果定义建议")
                .content(sb.toString())
                .highlights(highlights)
                .build();
    }

    private String assessOverallRisk(CustomerProfile p) {
        int riskScore = 0;

        if (p.getTotalStaff() != null && p.getTotalStaff() > 500) riskScore += 2;
        else if (p.getTotalStaff() != null && p.getTotalStaff() > 200) riskScore += 1;

        if (hasText(p.getExistingSystems())) riskScore += 2;

        if (p.getTotalProductionLines() != null && p.getTotalProductionLines() > 3) riskScore += 2;

        if (p.getTotalWarehouseCount() != null && p.getTotalWarehouseCount() > 5) riskScore += 1;

        if (hasText(p.getDecisionChain()) && p.getDecisionChain().length() > 20) riskScore += 1;

        return riskScore >= 5 ? "HIGH" : riskScore >= 3 ? "MEDIUM" : "LOW";
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
