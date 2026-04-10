package com.aimpl.domain.research.service;

import com.aimpl.domain.research.dto.QuestionnaireSectionVO;
import com.aimpl.domain.research.dto.QuestionVO;
import com.aimpl.domain.research.dto.QuestionnaireVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class QuestionnaireService {

    public QuestionnaireVO generate(String industryType, String scale, List<String> modules) {
        String typeLabel = resolveTypeLabel(industryType);
        AtomicInteger idSeq = new AtomicInteger(1);

        List<QuestionnaireSectionVO> sections = new ArrayList<>();
        sections.add(buildBusinessModelSection(idSeq));
        sections.add(buildCategorySection(idSeq));
        sections.add(buildProductionSection(industryType, idSeq));
        sections.add(buildWarehouseSection(industryType, idSeq));
        sections.add(buildSalesSection(idSeq));
        sections.add(buildCustomerBaseSection(idSeq));
        sections.add(buildOrganizationSection(scale, idSeq));
        sections.add(buildItSystemSection(idSeq));
        sections.add(buildTargetModuleSection(modules, idSeq));
        sections.add(buildGoalSection(idSeq));

        return QuestionnaireVO.builder()
                .title(typeLabel + "企业调研问卷")
                .industryType(industryType)
                .sections(sections)
                .build();
    }

    private QuestionnaireSectionVO buildBusinessModelSection(AtomicInteger seq) {
        List<QuestionVO> questions = new ArrayList<>();
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("企业经营类型")
                .questionType("SELECT").required(true)
                .options(List.of("直营", "代理", "混合")).hint("选择主要经营类型").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("贸易范围")
                .questionType("SELECT").required(true)
                .options(List.of("内贸", "外贸", "内外贸")).hint("选择贸易覆盖范围").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("主营业务描述")
                .questionType("TEXT").required(true)
                .hint("请简要描述公司主营业务").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("法定代表人")
                .questionType("TEXT").required(false)
                .hint("请填写法定代表人姓名").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("注册资本")
                .questionType("TEXT").required(false)
                .hint("例如: 5000万元").build());

        return QuestionnaireSectionVO.builder()
                .sectionName("经营模式")
                .sectionDescription("了解企业基本经营模式与贸易范围")
                .questions(questions).build();
    }

    private QuestionnaireSectionVO buildCategorySection(AtomicInteger seq) {
        List<QuestionVO> questions = new ArrayList<>();
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("主营品类")
                .questionType("MULTI_SELECT").required(true)
                .options(List.of("板材", "型材", "管材", "线材", "带钢", "不锈钢", "特钢", "其他"))
                .hint("可多选").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("经营品种明细")
                .questionType("TEXT").required(false)
                .hint("请列出具体品种/规格，用逗号分隔").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("合作钢厂/品牌")
                .questionType("TEXT").required(false)
                .hint("请列出主要合作钢厂或品牌").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("各品类销售占比")
                .questionType("TEXT").required(false)
                .hint("例如: 板材60%,型材30%,管材10%").build());

        return QuestionnaireSectionVO.builder()
                .sectionName("经营品类")
                .sectionDescription("了解企业经营的钢材品类分布")
                .questions(questions).build();
    }

    private QuestionnaireSectionVO buildProductionSection(String industryType, AtomicInteger seq) {
        List<QuestionVO> questions = new ArrayList<>();
        boolean detailed = "STEEL_MILL".equals(industryType) || "PROCESSING_CENTER".equals(industryType);

        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("产线/加工线数量")
                .questionType("NUMBER").required(detailed)
                .hint("请填写产线或加工线总数").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("产线类型")
                .questionType("TEXT").required(detailed)
                .hint("例如: 热轧线、冷轧线、开平线、纵剪线").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("日/月产能")
                .questionType("TEXT").required(detailed)
                .hint("请填写产能数据，注明单位").build());

        if (detailed) {
            questions.add(QuestionVO.builder()
                    .questionId(nextId(seq)).questionText("排班模式")
                    .questionType("SELECT").required(true)
                    .options(List.of("两班", "三班", "单班"))
                    .hint("选择当前排班模式").build());
            questions.add(QuestionVO.builder()
                    .questionId(nextId(seq)).questionText("MES系统现状")
                    .questionType("SELECT").required(true)
                    .options(List.of("无", "部分上线", "已有完整MES"))
                    .hint("选择MES系统使用情况").build());
            questions.add(QuestionVO.builder()
                    .questionId(nextId(seq)).questionText("质量管理标准")
                    .questionType("MULTI_SELECT").required(true)
                    .options(List.of("ISO9001", "IATF16949", "国标", "企标", "其他"))
                    .hint("选择适用的质量标准").build());
            questions.add(QuestionVO.builder()
                    .questionId(nextId(seq)).questionText("生产工艺流程概述")
                    .questionType("TEXT").required(false)
                    .hint("请描述核心生产工艺流程").build());
            questions.add(QuestionVO.builder()
                    .questionId(nextId(seq)).questionText("产品质检环节")
                    .questionType("TEXT").required(false)
                    .hint("请描述关键质检点和方式").build());
        }

        return QuestionnaireSectionVO.builder()
                .sectionName("生产能力")
                .sectionDescription("了解企业生产/加工能力及现状")
                .questions(questions).build();
    }

    private QuestionnaireSectionVO buildWarehouseSection(String industryType, AtomicInteger seq) {
        List<QuestionVO> questions = new ArrayList<>();
        boolean detailed = "STEEL_TRADER".equals(industryType);

        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("仓库数量")
                .questionType("NUMBER").required(true)
                .hint("请填写仓库总数（含自有和租用）").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("仓库总面积(㎡)")
                .questionType("NUMBER").required(true)
                .hint("请填写所有仓库合计面积").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("行车数量及吨位")
                .questionType("TEXT").required(true)
                .hint("例如: 5台,最大吨位20吨").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("库存周转率")
                .questionType("NUMBER").required(false)
                .hint("年库存周转次数").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("库存管理方式")
                .questionType("SELECT").required(true)
                .options(List.of("手工台账", "Excel", "WMS系统", "ERP库存模块"))
                .hint("选择当前库存管理方式").build());

        if (detailed) {
            questions.add(QuestionVO.builder()
                    .questionId(nextId(seq)).questionText("仓库类型分布")
                    .questionType("TEXT").required(false)
                    .hint("例如: 自有仓3个,租用仓2个,钢厂仓1个").build());
            questions.add(QuestionVO.builder()
                    .questionId(nextId(seq)).questionText("仓储费用结构")
                    .questionType("TEXT").required(false)
                    .hint("描述仓储成本构成").build());
            questions.add(QuestionVO.builder()
                    .questionId(nextId(seq)).questionText("库存盘点频率")
                    .questionType("SELECT").required(false)
                    .options(List.of("每日", "每周", "每月", "每季度"))
                    .hint("选择盘点频率").build());
        }

        return QuestionnaireSectionVO.builder()
                .sectionName("库存与仓储")
                .sectionDescription("了解企业仓储物流管理现状")
                .questions(questions).build();
    }

    private QuestionnaireSectionVO buildSalesSection(AtomicInteger seq) {
        List<QuestionVO> questions = new ArrayList<>();
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("月均销量(吨)")
                .questionType("NUMBER").required(true)
                .hint("请填写近半年月均销量").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("月均销售额(万元)")
                .questionType("NUMBER").required(true)
                .hint("请填写近半年月均销售额").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("定价模式")
                .questionType("SELECT").required(true)
                .options(List.of("一口价", "基价+加工费", "期货联动", "协议价", "混合"))
                .hint("选择主要定价模式").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("结算方式")
                .questionType("MULTI_SELECT").required(true)
                .options(List.of("现款现货", "货到付款", "账期", "承兑汇票", "信用证", "其他"))
                .hint("可多选").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("信用政策")
                .questionType("TEXT").required(false)
                .hint("描述授信额度、账期等政策").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("销售模式")
                .questionType("SELECT").required(true)
                .options(List.of("现货批发", "期货", "零售", "混合"))
                .hint("选择主要销售模式").build());

        return QuestionnaireSectionVO.builder()
                .sectionName("销售状况")
                .sectionDescription("了解企业销售规模与商务模式")
                .questions(questions).build();
    }

    private QuestionnaireSectionVO buildCustomerBaseSection(AtomicInteger seq) {
        List<QuestionVO> questions = new ArrayList<>();
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("客户总数")
                .questionType("NUMBER").required(true)
                .hint("请填写活跃客户总数").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("客户类型分布")
                .questionType("MULTI_SELECT").required(true)
                .options(List.of("终端用户", "贸易商", "加工厂", "工程项目", "出口客户", "其他"))
                .hint("可多选").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("主要大客户")
                .questionType("TEXT").required(false)
                .hint("列出前5大客户名称，逗号分隔").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("大客户销售占比")
                .questionType("TEXT").required(false)
                .hint("前5大客户合计销售占比").build());

        return QuestionnaireSectionVO.builder()
                .sectionName("客户群体")
                .sectionDescription("了解企业客户结构与分布")
                .questions(questions).build();
    }

    private QuestionnaireSectionVO buildOrganizationSection(String scale, AtomicInteger seq) {
        List<QuestionVO> questions = new ArrayList<>();
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("部门设置")
                .questionType("TEXT").required(true)
                .hint("列出主要部门名称，逗号分隔").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("员工总数")
                .questionType("NUMBER").required(true)
                .hint("请填写在职员工总数").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("关键岗位")
                .questionType("TEXT").required(true)
                .hint("列出与系统相关的关键岗位，逗号分隔").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("决策链路")
                .questionType("TEXT").required(false)
                .hint("描述采购/销售审批流程的决策链路").build());

        if ("大型".equals(scale) || "LARGE".equals(scale)) {
            questions.add(QuestionVO.builder()
                    .questionId(nextId(seq)).questionText("分支机构数量")
                    .questionType("NUMBER").required(false)
                    .hint("请填写分支机构/分公司数量").build());
        }

        return QuestionnaireSectionVO.builder()
                .sectionName("组织架构")
                .sectionDescription("了解企业组织结构与人员配置")
                .questions(questions).build();
    }

    private QuestionnaireSectionVO buildItSystemSection(AtomicInteger seq) {
        List<QuestionVO> questions = new ArrayList<>();
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("当前使用的信息系统")
                .questionType("TEXT").required(true)
                .hint("列出所有在用系统名称").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("系统供应商")
                .questionType("TEXT").required(false)
                .hint("列出各系统对应的供应商").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("现有系统痛点")
                .questionType("TEXT").required(true)
                .hint("描述现有系统使用中的主要问题").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("数据现状")
                .questionType("SELECT").required(true)
                .options(List.of("无历史数据", "Excel数据为主", "有系统数据可导出", "多系统数据需整合"))
                .hint("选择当前数据管理状态").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("IT人员配备")
                .questionType("SELECT").required(false)
                .options(List.of("无专职IT", "1-2人", "3-5人", "IT部门"))
                .hint("选择IT人员情况").build());

        return QuestionnaireSectionVO.builder()
                .sectionName("现有IT系统")
                .sectionDescription("了解企业信息化现状与痛点")
                .questions(questions).build();
    }

    private QuestionnaireSectionVO buildTargetModuleSection(List<String> modules, AtomicInteger seq) {
        List<QuestionVO> questions = new ArrayList<>();

        List<String> moduleOptions = (modules != null && !modules.isEmpty())
                ? modules
                : List.of("进销存", "财务", "仓储", "生产", "质量", "物流", "OA", "CRM", "电商", "BI报表");

        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("目标上线模块")
                .questionType("MULTI_SELECT").required(true)
                .options(moduleOptions)
                .hint("选择计划上线的功能模块").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("模块优先级排序")
                .questionType("TEXT").required(true)
                .hint("按优先级列出模块名称，逗号分隔").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("期望上线时间")
                .questionType("TEXT").required(false)
                .hint("请填写期望的系统上线日期").build());

        return QuestionnaireSectionVO.builder()
                .sectionName("目标模块需求")
                .sectionDescription("明确企业信息化建设目标与模块需求")
                .questions(questions).build();
    }

    private QuestionnaireSectionVO buildGoalSection(AtomicInteger seq) {
        List<QuestionVO> questions = new ArrayList<>();
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("管理目标")
                .questionType("TEXT").required(true)
                .hint("期望通过系统实现的管理改善目标").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("流程优化目标")
                .questionType("TEXT").required(false)
                .hint("期望优化的业务流程").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("效率提升目标")
                .questionType("TEXT").required(false)
                .hint("期望提升效率的具体环节和量化指标").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("风险管控目标")
                .questionType("TEXT").required(false)
                .hint("期望加强的风险控制领域").build());
        questions.add(QuestionVO.builder()
                .questionId(nextId(seq)).questionText("其他需求与期望")
                .questionType("TEXT").required(false)
                .hint("任何补充说明或特殊需求").build());

        return QuestionnaireSectionVO.builder()
                .sectionName("管理诉求与目标")
                .sectionDescription("了解企业管理改善诉求与项目目标")
                .questions(questions).build();
    }

    private String nextId(AtomicInteger seq) {
        return "Q" + seq.getAndIncrement();
    }

    private String resolveTypeLabel(String industryType) {
        return switch (industryType) {
            case "STEEL_MILL" -> "钢厂";
            case "STEEL_TRADER" -> "钢贸商";
            case "PROCESSING_CENTER" -> "加工中心";
            case "INTEGRATED_SERVICE" -> "综合服务商";
            default -> industryType;
        };
    }
}
