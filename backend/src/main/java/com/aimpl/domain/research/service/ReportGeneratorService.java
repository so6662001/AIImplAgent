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

    // ═══ 第1章：企业概况与行业定位 ═══

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
        sb.append("。\n");

        if (hasText(p.getMainBusiness())) {
            sb.append("主营业务：").append(p.getMainBusiness()).append("。\n");
        }
        if (p.getTotalStaff() != null && p.getTotalStaff() > 0) {
            sb.append("现有员工").append(p.getTotalStaff()).append("人");
            if (hasText(p.getDepartments())) {
                sb.append("，下设").append(p.getDepartments());
            }
            sb.append("。\n");
        }
        if (hasText(p.getKeyPositions())) {
            sb.append("关键岗位包括：").append(p.getKeyPositions()).append("。\n");
        }
        if (hasText(p.getDecisionChain())) {
            sb.append("企业决策链：").append(p.getDecisionChain()).append("。\n");
        }

        sb.append("\n【行业定位分析】\n");
        switch (typeLabel) {
            case "钢贸商" -> sb.append("该企业定位于钢材流通领域，核心竞争力在于渠道资源和库存周转效率。系统建设应重点关注进销存管理、库存优化和客户管理。");
            case "钢厂" -> sb.append("该企业定位于钢材生产制造领域，核心竞争力在于产能规模和产品质量。系统建设应重点关注MES生产管理、质量追溯和成本核算。");
            case "加工中心" -> sb.append("该企业定位于钢材加工服务领域，核心竞争力在于加工能力和交期管控。系统建设应重点关注加工排产、来料管理和加工成本。");
            case "综合服务商" -> sb.append("该企业定位于钢铁产业链综合服务，业务多元化程度高。系统建设需兼顾贸易、加工、仓储等多业态协同管理。");
            default -> sb.append("建议进一步明确企业的行业定位和核心竞争力。");
        }

        List<String> highlights = new ArrayList<>();
        highlights.add("行业定位：" + typeLabel);
        if (p.getTotalStaff() != null && p.getTotalStaff() > 0) {
            String scale = p.getTotalStaff() > 200 ? "大型" : p.getTotalStaff() > 50 ? "中型" : "小型";
            highlights.add("企业规模：" + scale + "(" + p.getTotalStaff() + "人)");
        }
        if (hasText(p.getMainBusiness())) highlights.add("主营业务：" + p.getMainBusiness());
        if (hasText(p.getDepartments())) highlights.add("组织架构：" + p.getDepartments());

        return ReportSectionVO.builder()
                .sectionTitle("企业概况与行业定位")
                .content(sb.toString())
                .highlights(highlights)
                .build();
    }

    // ═══ 第2章：经营模式分析与建议 ═══

    private ReportSectionVO buildBusinessModelAnalysis(CustomerProfile p) {
        StringBuilder sb = new StringBuilder();

        sb.append("【经营模式现状】\n");
        if (p.getBusinessModel() != null) {
            sb.append("经营模式：").append(p.getBusinessModel().getLabel()).append("。");
        }
        if (p.getTradeMode() != null) {
            sb.append("贸易模式：").append(p.getTradeMode().getLabel()).append("。");
        }
        if (p.getTradeScope() != null) {
            sb.append("贸易范围：").append(p.getTradeScope().getLabel()).append("。");
        }
        sb.append("\n");

        sb.append("【销售状况】\n");
        if (hasText(p.getSalesMode())) sb.append("销售模式：").append(p.getSalesMode()).append("。");
        if (hasText(p.getPricingModel())) sb.append("定价模式：").append(p.getPricingModel()).append("。");
        if (hasText(p.getSettlementMethods())) sb.append("结算方式：").append(p.getSettlementMethods()).append("。");
        if (hasText(p.getCreditPolicy())) sb.append("信用政策：").append(p.getCreditPolicy()).append("。");
        if (p.getMonthlyVolume() != null) sb.append("月均销量：").append(p.getMonthlyVolume()).append("吨。");
        if (p.getMonthlyAmount() != null) sb.append("月均销售额：").append(p.getMonthlyAmount()).append("万元。");
        sb.append("\n");

        if (p.getTotalCustomerCount() != null && p.getTotalCustomerCount() > 0) {
            sb.append("【客户群体】\n");
            sb.append("客户总数：").append(p.getTotalCustomerCount()).append("家。");
            if (hasText(p.getCustomerTypes())) sb.append("客户类型分布：").append(p.getCustomerTypes()).append("。");
            if (hasText(p.getTopCustomers())) sb.append("主要大客户：").append(p.getTopCustomers()).append("。");
            sb.append("\n");
        }

        sb.append("\n【经营模式优化建议】\n");
        List<String> suggestions = new ArrayList<>();
        if (p.getMonthlyVolume() != null && p.getMonthlyAmount() != null && p.getMonthlyVolume().doubleValue() > 0) {
            double avgPrice = p.getMonthlyAmount().doubleValue() * 10000 / p.getMonthlyVolume().doubleValue();
            suggestions.add(String.format("当前吨均价约%.0f元/吨，建议通过系统实现价格趋势分析，优化采销决策", avgPrice));
        }
        if (hasText(p.getSettlementMethods()) && p.getSettlementMethods().contains("月结")) {
            suggestions.add("存在月结结算方式，建议系统建立客户信用额度管控和应收账龄预警机制");
        }
        if (p.getTotalCustomerCount() != null && p.getTotalCustomerCount() > 100) {
            suggestions.add("客户数量较多(" + p.getTotalCustomerCount() + "家)，建议通过系统实现客户分级管理和精准营销");
        }
        if (hasText(p.getSalesMode()) && p.getSalesMode().contains("现货")) {
            suggestions.add("现货批发模式对库存周转要求高，建议重点优化库存预警和智能补货功能");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("建议在调研阶段进一步了解客户经营模式细节，以便提供更精准的优化建议");
        }
        for (int i = 0; i < suggestions.size(); i++) {
            sb.append(i + 1).append(". ").append(suggestions.get(i)).append("\n");
        }

        List<String> highlights = new ArrayList<>();
        if (p.getBusinessModel() != null) highlights.add("经营模式：" + p.getBusinessModel().getLabel());
        if (hasText(p.getSalesMode())) highlights.add("销售模式：" + p.getSalesMode());
        if (p.getTotalCustomerCount() != null) highlights.add("客户数量：" + p.getTotalCustomerCount() + "家");
        highlights.add("优化建议：" + suggestions.size() + "条");

        return ReportSectionVO.builder()
                .sectionTitle("经营模式分析与建议")
                .content(sb.toString())
                .highlights(highlights)
                .build();
    }

    // ═══ 第3章：业务流程梳理 ═══

    private ReportSectionVO buildBusinessProcessAnalysis(CustomerProfile p) {
        StringBuilder sb = new StringBuilder();
        List<String> highlights = new ArrayList<>();

        String type = p.getIndustryType() != null ? p.getIndustryType().name() : "";

        sb.append("【核心业务流程】\n");
        switch (type) {
            case "STEEL_MILL" -> {
                sb.append("┌─────────────────────────────────────────────────────────────┐\n");
                sb.append("│ 原材料采购 → 来料质检 → 生产排产 → 生产执行 → 成品质检     │\n");
                sb.append("│     → 成品入库 → 销售开单 → 出库发货 → 物流跟踪 → 结算对账 │\n");
                sb.append("└─────────────────────────────────────────────────────────────┘\n\n");
                sb.append("【生产环节详述】\n");
                if (p.getTotalProductionLines() != null && p.getTotalProductionLines() > 0) {
                    sb.append("当前拥有").append(p.getTotalProductionLines()).append("条产线");
                    if (hasText(p.getProductionShifts())) sb.append("，实行").append(p.getProductionShifts()).append("制");
                    sb.append("。\n");
                }
                if (hasText(p.getMesCurrentStatus())) {
                    sb.append("MES系统现状：").append(p.getMesCurrentStatus());
                    if ("无".equals(p.getMesCurrentStatus())) {
                        sb.append("，建议本次项目重点导入MES模块，实现生产过程数字化管控");
                    } else if ("部分".equals(p.getMesCurrentStatus())) {
                        sb.append("，建议评估现有MES与新系统的集成方案");
                    }
                    sb.append("。\n");
                }
                if (hasText(p.getQualityStandards())) sb.append("执行质量标准：").append(p.getQualityStandards()).append("。\n");
                highlights.add("核心流程：采购→生产→质检→销售→结算");
                if (p.getTotalProductionLines() != null) highlights.add("产线：" + p.getTotalProductionLines() + "条");
            }
            case "STEEL_TRADER" -> {
                sb.append("┌─────────────────────────────────────────────────────────────┐\n");
                sb.append("│ 采购询价 → 采购下单 → 到货验收 → 入库上架 → 库存管理       │\n");
                sb.append("│     → 销售报价 → 销售开单 → 出库发货 → 物流配送 → 结算对账 │\n");
                sb.append("└─────────────────────────────────────────────────────────────┘\n\n");
                sb.append("【仓储物流环节详述】\n");
                if (p.getTotalWarehouseCount() != null && p.getTotalWarehouseCount() > 0) {
                    sb.append("拥有").append(p.getTotalWarehouseCount()).append("个仓库");
                    if (p.getTotalWarehouseAreaSqm() != null) sb.append("，总面积").append(p.getTotalWarehouseAreaSqm()).append("㎡");
                    sb.append("。\n");
                }
                if (p.getTotalCraneCount() != null && p.getTotalCraneCount() > 0) {
                    sb.append("配备行车").append(p.getTotalCraneCount()).append("台，需在系统中实现行车调度和计量对接。\n");
                }
                if (p.getInventoryTurnoverRate() != null) {
                    sb.append("当前库存周转率：").append(p.getInventoryTurnoverRate());
                    if (p.getInventoryTurnoverRate().doubleValue() < 6) {
                        sb.append("（偏低，建议通过系统优化库存结构，提升周转效率）");
                    }
                    sb.append("。\n");
                }
                if (hasText(p.getInventoryManagementMethod())) {
                    sb.append("现有管理方式：").append(p.getInventoryManagementMethod()).append("。\n");
                }
                highlights.add("核心流程：采购→入库→库存→销售→物流→结算");
                if (p.getTotalWarehouseCount() != null) highlights.add("仓库：" + p.getTotalWarehouseCount() + "个");
                if (p.getTotalCraneCount() != null && p.getTotalCraneCount() > 0) highlights.add("行车：" + p.getTotalCraneCount() + "台");
            }
            case "PROCESSING_CENTER" -> {
                sb.append("┌─────────────────────────────────────────────────────────────┐\n");
                sb.append("│ 客户下单 → 来料接收 → 来料质检 → 加工排产 → 加工执行       │\n");
                sb.append("│     → 成品质检 → 成品入库 → 交付出库 → 结算对账             │\n");
                sb.append("└─────────────────────────────────────────────────────────────┘\n\n");
                if (p.getTotalProductionLines() != null && p.getTotalProductionLines() > 0) {
                    sb.append("拥有").append(p.getTotalProductionLines()).append("条加工线，需在系统中实现加工工单管理和排产优化。\n");
                }
                highlights.add("核心流程：来料→加工→质检→交付→结算");
            }
            default -> {
                sb.append("┌─────────────────────────────────────────────────────────────┐\n");
                sb.append("│ 采购管理 → 库存管理 → 销售管理 → 加工服务                   │\n");
                sb.append("│     → 仓储物流 → 财务结算 → 报表分析                       │\n");
                sb.append("└─────────────────────────────────────────────────────────────┘\n\n");
                highlights.add("核心流程：采购→库存→销售→加工→物流→结算");
            }
        }

        if (hasText(p.getTargetModules())) {
            sb.append("\n【系统覆盖范围】\n");
            sb.append("目标上线模块：").append(p.getTargetModules()).append("\n");
            sb.append("系统需全面覆盖上述核心业务流程，实现业务数据线上化和流程标准化。\n");
        }

        if (hasText(p.getExistingSystems())) {
            sb.append("\n【现有系统现状】\n");
            sb.append("当前在用系统：").append(p.getExistingSystems()).append("\n");
            sb.append("需评估现有系统与新系统的数据迁移方案和过渡期并行策略。\n");
            highlights.add("存在系统迁移需求");
        }

        return ReportSectionVO.builder()
                .sectionTitle("业务流程梳理")
                .content(sb.toString())
                .highlights(highlights)
                .build();
    }

    // ═══ 第4章：项目目标总结与优先级建议 ═══

    private ReportSectionVO buildGoalSummary(CustomerProfile p) {
        StringBuilder sb = new StringBuilder();
        List<String> highlights = new ArrayList<>();
        int goalCount = 0;

        sb.append("【项目目标梳理】\n\n");
        if (hasText(p.getManagementGoals())) {
            sb.append("一、管理目标：").append(p.getManagementGoals()).append("\n");
            highlights.add("管理目标已明确");
            goalCount++;
        }
        if (hasText(p.getProcessGoals())) {
            sb.append("二、流程优化目标：").append(p.getProcessGoals()).append("\n");
            highlights.add("流程目标已明确");
            goalCount++;
        }
        if (hasText(p.getEfficiencyGoals())) {
            sb.append("三、效率提升目标：").append(p.getEfficiencyGoals()).append("\n");
            highlights.add("效率目标已明确");
            goalCount++;
        }
        if (hasText(p.getRiskControlGoals())) {
            sb.append("四、风控目标：").append(p.getRiskControlGoals()).append("\n");
            highlights.add("风控目标已明确");
            goalCount++;
        }

        if (goalCount == 0) {
            sb.append("项目目标尚未明确，建议在调研阶段进一步与客户沟通确认。\n");
            highlights.add("建议：尽快明确项目目标");
        }

        sb.append("\n【模块优先级建议】\n");
        if (hasText(p.getModulePriorities())) {
            sb.append("客户期望的模块优先级：").append(p.getModulePriorities()).append("\n\n");
        }

        String type = p.getIndustryType() != null ? p.getIndustryType().name() : "";
        sb.append("基于行业特点和项目目标，建议实施优先级如下：\n");
        switch (type) {
            case "STEEL_TRADER" -> {
                sb.append("第一优先级：进销存（核心业务支撑）\n");
                sb.append("第二优先级：仓储管理（提升库存周转效率）\n");
                sb.append("第三优先级：财务模块（应收应付+结算对账）\n");
                sb.append("第四优先级：智能分析（报表+决策支持）\n");
                highlights.add("建议优先上线：进销存→仓储→财务");
            }
            case "STEEL_MILL" -> {
                sb.append("第一优先级：MES生产管理（生产执行核心）\n");
                sb.append("第二优先级：进销存（采购和销售管理）\n");
                sb.append("第三优先级：质量管理（质量追溯体系）\n");
                sb.append("第四优先级：财务+成本核算\n");
                highlights.add("建议优先上线：MES→进销存→质量");
            }
            case "PROCESSING_CENTER" -> {
                sb.append("第一优先级：加工管理（工单+排产）\n");
                sb.append("第二优先级：进销存（来料+成品管理）\n");
                sb.append("第三优先级：财务结算\n");
                highlights.add("建议优先上线：加工管理→进销存→财务");
            }
            default -> {
                sb.append("第一优先级：进销存（核心业务支撑）\n");
                sb.append("第二优先级：仓储管理+加工管理\n");
                sb.append("第三优先级：财务+报表分析\n");
                highlights.add("建议优先上线：进销存→仓储/加工→财务");
            }
        }

        return ReportSectionVO.builder()
                .sectionTitle("项目目标总结与优先级建议")
                .content(sb.toString())
                .highlights(highlights)
                .build();
    }

    // ═══ 第5章：交付风险预评估 ═══

    private ReportSectionVO buildRiskAssessment(CustomerProfile p) {
        StringBuilder sb = new StringBuilder();
        List<String> highlights = new ArrayList<>();
        int riskScore = 0;

        sb.append("【风险评估明细】\n\n");

        if (p.getTotalStaff() != null && p.getTotalStaff() > 500) {
            sb.append("⚠ 【组织规模风险-高】员工").append(p.getTotalStaff()).append("人，属大型企业。");
            sb.append("培训覆盖范围广，系统推广难度高。");
            if (hasText(p.getDepartments())) {
                sb.append("涉及部门：").append(p.getDepartments()).append("。");
            }
            sb.append("\n  应对措施：建议分批次培训，先培训关键用户再逐步推广至全员。\n\n");
            highlights.add("高风险：大型企业培训推广");
            riskScore += 2;
        } else if (p.getTotalStaff() != null && p.getTotalStaff() > 200) {
            sb.append("△ 【组织规模风险-中】员工").append(p.getTotalStaff()).append("人，属中型企业。");
            sb.append("需制定分阶段培训计划。\n");
            sb.append("  应对措施：制定KA用户先行策略，确保关键岗位优先掌握系统操作。\n\n");
            riskScore += 1;
        }

        if (hasText(p.getExistingSystems())) {
            sb.append("⚠ 【系统迁移风险-高】现有系统：").append(p.getExistingSystems()).append("。\n");
            sb.append("  需评估历史数据迁移方案和新旧系统切换策略。\n");
            sb.append("  应对措施：制定详细数据迁移计划，安排并行运行过渡期，确保数据完整性。\n\n");
            highlights.add("高风险：系统迁移");
            riskScore += 2;
        }

        if (p.getTotalProductionLines() != null && p.getTotalProductionLines() > 3) {
            sb.append("⚠ 【生产复杂度风险-高】").append(p.getTotalProductionLines()).append("条产线，MES集成复杂度较高。\n");
            if (hasText(p.getMesCurrentStatus()) && "无".equals(p.getMesCurrentStatus())) {
                sb.append("  且当前无MES系统，属全新导入，实施难度更高。\n");
                riskScore += 1;
            }
            sb.append("  应对措施：安排MES专项顾问，分产线逐步上线，先试点后推广。\n\n");
            highlights.add("高风险：多产线MES集成");
            riskScore += 2;
        }

        if (p.getTotalWarehouseCount() != null && p.getTotalWarehouseCount() > 5) {
            sb.append("△ 【多仓管理风险-中】").append(p.getTotalWarehouseCount()).append("个仓库");
            if (p.getTotalWarehouseAreaSqm() != null) sb.append("(总面积").append(p.getTotalWarehouseAreaSqm()).append("㎡)");
            sb.append("，多仓协同管理需重点关注。\n");
            if (p.getTotalCraneCount() != null && p.getTotalCraneCount() > 0) {
                sb.append("  配备").append(p.getTotalCraneCount()).append("台行车，需确保计量系统对接。\n");
            }
            sb.append("  应对措施：统一仓库编码规范，逐仓上线，确保库位管理规则一致。\n\n");
            highlights.add("中风险：多仓协同管理");
            riskScore += 1;
        }

        if (p.getInventoryTurnoverRate() != null && p.getInventoryTurnoverRate().doubleValue() < 4) {
            sb.append("△ 【库存效率风险-中】库存周转率仅").append(p.getInventoryTurnoverRate()).append("，偏低。\n");
            sb.append("  应对措施：系统上线后重点优化库存预警和呆滞库存处理流程。\n\n");
            riskScore += 1;
        }

        if (hasText(p.getDecisionChain()) && p.getDecisionChain().length() > 20) {
            sb.append("△ 【决策效率风险-中】决策链较长（").append(p.getDecisionChain()).append("），项目推进可能受审批流程影响。\n");
            sb.append("  应对措施：提前获得高层支持，明确项目决策授权人，建立快速决策通道。\n\n");
            riskScore += 1;
        }

        if (p.getTotalCustomerCount() != null && p.getTotalCustomerCount() > 500) {
            sb.append("△ 【数据量风险-中】客户数量").append(p.getTotalCustomerCount()).append("家，期初数据迁移量大。\n");
            sb.append("  应对措施：提前启动数据整理，利用AI数据治理工具辅助清洗和校验。\n\n");
            riskScore += 1;
        }

        if (riskScore == 0) {
            sb.append("基于当前已知信息，项目整体风险可控。建议在调研阶段进一步识别潜在风险。\n");
            highlights.add("整体风险可控");
        }

        String level = riskScore >= 5 ? "HIGH" : riskScore >= 3 ? "MEDIUM" : "LOW";
        sb.append("\n【综合风险评级】").append(level);
        sb.append(" (风险因子得分：").append(riskScore).append("/10)\n");
        highlights.add("风险评级：" + level);

        return ReportSectionVO.builder()
                .sectionTitle("交付风险预评估")
                .content(sb.toString())
                .highlights(highlights)
                .build();
    }

    // ═══ 第6章：项目成果定义建议 ═══

    private ReportSectionVO buildDeliverableDefinition(CustomerProfile p) {
        StringBuilder sb = new StringBuilder();
        List<String> highlights = new ArrayList<>();

        sb.append("【系统交付成果】\n\n");

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

        sb.append("\n【配套交付物清单】\n");
        sb.append("├─ 《客户背景分析报告》\n");
        sb.append("├─ 《项目交付计划》(含WBS、里程碑、甘特图)\n");
        sb.append("├─ 《系统操作手册》(按岗位角色分册)\n");
        sb.append("├─ 《业务流程规范文档》\n");
        sb.append("├─ 《数据迁移报告》(含清洗校验记录)\n");
        sb.append("├─ 《培训记录》(含签到、考核成绩、学员画像)\n");
        sb.append("├─ 《模拟演练评估报告》\n");
        sb.append("├─ 《上线准备度评估报告》\n");
        sb.append("└─ 《项目交付验收报告》\n");

        sb.append("\n【培训交付标准】\n");
        if (p.getTotalStaff() != null && p.getTotalStaff() > 0) {
            sb.append("培训对象：").append(p.getTotalStaff()).append("人\n");
            if (hasText(p.getKeyPositions())) {
                sb.append("关键用户岗位：").append(p.getKeyPositions()).append("\n");
            }
        }
        sb.append("KA用户考核标准：每门必学课程≥70分，综合≥75分\n");
        sb.append("普通用户培训覆盖率：≥90%\n");
        sb.append("上线前提：全部KA用户通过考核方可启用帐套\n");

        sb.append("\n【验收标准建议】\n");
        sb.append("1. 系统功能验收：全部上线模块功能正常运行\n");
        sb.append("2. 数据验收：期初数据导入完成，勾稽关系校验通过\n");
        sb.append("3. 培训验收：KA用户全部通过考核\n");
        sb.append("4. 运行验收：系统正式运行≥1个月，核心业务流程走通\n");
        sb.append("5. 文档验收：全套交付文档完整归档\n");

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
        if (p.getTotalCustomerCount() != null && p.getTotalCustomerCount() > 500) riskScore += 1;
        if (p.getInventoryTurnoverRate() != null && p.getInventoryTurnoverRate().doubleValue() < 4) riskScore += 1;
        return riskScore >= 5 ? "HIGH" : riskScore >= 3 ? "MEDIUM" : "LOW";
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
