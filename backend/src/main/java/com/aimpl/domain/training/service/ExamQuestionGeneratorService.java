package com.aimpl.domain.training.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.training.vo.ExamQuestionVO;
import com.aimpl.domain.training.vo.GeneratedExamVO;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExamQuestionGeneratorService {

    private static final Map<String, List<String[]>> QUESTION_BANK = new LinkedHashMap<>();
    private static final List<String> VALID_DIFFICULTIES = List.of("基础级", "进阶级", "综合级");

    static {
        QUESTION_BANK.put("采购管理", List.of(
                new String[]{"CHOICE", "在采购订单中，以下哪项是必填字段？", "A.供应商名称|B.联系电话|C.传真号码|D.邮编", "A", "采购订单基本要素"},
                new String[]{"CHOICE", "供应商选择时，以下哪个不是主要评估维度？", "A.价格|B.质量|C.办公装修|D.交货周期", "C", "供应商评估标准"},
                new String[]{"CHOICE", "比价单的作用是什么？", "A.记录员工出勤|B.比较不同供应商报价|C.记录库存变动|D.生成财务报表", "B", "比价流程"},
                new String[]{"CHOICE", "采购退货时需要关联的单据是？", "A.销售订单|B.采购入库单|C.盘点单|D.调拨单", "B", "采购退货流程"},
                new String[]{"TRUE_FALSE", "采购订单审核后不可修改数量", "正确|错误", "正确", "审核规则"},
                new String[]{"TRUE_FALSE", "一张采购订单只能对应一个供应商", "正确|错误", "正确", "订单规则"},
                new String[]{"OPERATION", "请描述完整的采购入库操作流程，从创建采购订单到入库确认", "", "创建采购订单→审核→到货检验→采购入库→确认", "采购入库全流程"},
                new String[]{"OPERATION", "请操作：创建一张采购订单，选择供应商并添加商品明细", "", "进入采购管理→新增采购订单→选择供应商→添加商品→保存", "采购订单创建"},
                new String[]{"OPERATION", "请演示如何进行采购比价操作", "", "进入比价管理→新增比价单→添加供应商报价→比较→确认最优", "比价操作"},
                new String[]{"SCENARIO", "供应商A报价100元/吨但交期30天，供应商B报价105元/吨但交期15天，客户急需用料，如何决策？", "", "综合考虑交期紧急程度选择供应商B，差价可通过谈判或后续合作弥补", "供应商决策"}
        ));
        QUESTION_BANK.put("销售管理", List.of(
                new String[]{"CHOICE", "销售订单中客户信用额度的作用是？", "A.限制采购金额|B.控制客户赊购上限|C.计算员工绩效|D.统计库存数量", "B", "信用管理"},
                new String[]{"CHOICE", "销售定价策略中，以下哪种是按客户等级定价？", "A.统一定价|B.分级定价|C.成本定价|D.随机定价", "B", "定价策略"},
                new String[]{"CHOICE", "销售退货需要关联的原始单据是？", "A.采购订单|B.销售出库单|C.盘点单|D.调拨单", "B", "退货流程"},
                new String[]{"CHOICE", "客户对账单的主要用途是？", "A.内部审计|B.与客户核对往来款项|C.统计库存|D.采购参考", "B", "对账管理"},
                new String[]{"TRUE_FALSE", "销售出库前必须先审核销售订单", "正确|错误", "正确", "出库规则"},
                new String[]{"TRUE_FALSE", "同一客户可以设置多个收货地址", "正确|错误", "正确", "客户管理"},
                new String[]{"OPERATION", "请描述从销售订单到出库开票的完整流程", "", "创建销售订单→审核→安排出库→出库确认→开票", "销售全流程"},
                new String[]{"OPERATION", "请操作：为客户创建销售报价单并转为销售订单", "", "新增报价单→选择客户→添加商品→转为订单→保存", "报价转订单"},
                new String[]{"OPERATION", "请演示如何对超信用额度客户进行特批操作", "", "系统提示超额→提交特批申请→审批流程→特批通过→继续下单", "信用特批"},
                new String[]{"SCENARIO", "客户要求在原有订单基础上追加数量并享受原价，但当前价格已上涨5%，如何处理？", "", "与客户沟通价格变动原因，追加部分按新价执行或申请特殊审批维持原价", "价格变动处理"}
        ));
        QUESTION_BANK.put("库存管理", List.of(
                new String[]{"CHOICE", "盘点操作的主要目的是？", "A.统计员工人数|B.核实账面与实物库存差异|C.计算销售利润|D.检查设备状况", "B", "盘点目的"},
                new String[]{"CHOICE", "库存调整单通常在什么情况下使用？", "A.正常出入库|B.盘盈盘亏处理|C.采购退货|D.销售开票", "B", "库存调整"},
                new String[]{"CHOICE", "调拨单的作用是？", "A.仓库间货物转移|B.客户间转账|C.供应商结算|D.员工调岗", "A", "调拨管理"},
                new String[]{"CHOICE", "安全库存的含义是？", "A.仓库最大容量|B.防止缺货的最低库存量|C.每日出库量|D.年度采购量", "B", "安全库存"},
                new String[]{"TRUE_FALSE", "盘点时必须停止所有出入库操作", "正确|错误", "错误", "盘点规则"},
                new String[]{"TRUE_FALSE", "库存调拨需要在调出仓和调入仓分别确认", "正确|错误", "正确", "调拨确认"},
                new String[]{"OPERATION", "请描述完整的库存盘点操作流程", "", "创建盘点计划→生成盘点单→实物盘点→录入实盘数→差异分析→调整", "盘点流程"},
                new String[]{"OPERATION", "请操作仓库间调拨，从A仓调100吨螺纹钢到B仓", "", "新增调拨单→选择调出仓A→选择调入仓B→添加商品及数量→提交→确认", "调拨操作"},
                new String[]{"OPERATION", "请演示如何设置商品的安全库存预警", "", "进入库存设置→选择商品→设置安全库存量→设置预警方式→保存", "库存预警"},
                new String[]{"SCENARIO", "盘点发现某商品实物比账面多出50吨，可能原因有哪些？应如何处理？", "", "可能原因：入库多入未记录、退货未及时冲减等。处理：调查原因→盘盈调整→追溯单据", "盘盈处理"}
        ));
        QUESTION_BANK.put("财务应收", List.of(
                new String[]{"CHOICE", "应收账款账龄分析的目的是？", "A.统计库存|B.评估客户回款风险|C.计算采购成本|D.记录出勤", "B", "账龄分析"},
                new String[]{"CHOICE", "收款核销时应遵循的原则是？", "A.先进先出|B.随机核销|C.金额最大优先|D.不需要核销", "A", "核销原则"},
                new String[]{"CHOICE", "预收款在系统中应如何处理？", "A.直接冲减应收|B.挂预收账款科目|C.忽略不处理|D.退回客户", "B", "预收处理"},
                new String[]{"CHOICE", "坏账准备计提的依据是？", "A.销售额|B.账龄和客户信用|C.采购额|D.库存量", "B", "坏账管理"},
                new String[]{"TRUE_FALSE", "应收账款可以直接删除而无需做冲销处理", "正确|错误", "错误", "应收规则"},
                new String[]{"TRUE_FALSE", "同一笔收款可以核销多张应收单据", "正确|错误", "正确", "收款核销"},
                new String[]{"OPERATION", "请描述应收对账到收款核销的完整流程", "", "生成对账单→客户确认→收款登记→收款核销→确认", "应收全流程"},
                new String[]{"OPERATION", "请操作：登记一笔客户收款并核销对应的销售应收", "", "进入收款管理→新增收款→选择客户→选择核销单据→确认核销→保存", "收款登记"},
                new String[]{"OPERATION", "请演示如何查看客户账龄分析报表", "", "进入财务报表→选择账龄分析→设置查询条件→查看明细→导出", "账龄查询"},
                new String[]{"SCENARIO", "某客户120天以上应收余额占比超过40%，应采取哪些催收措施？", "", "电话催收→发送催款函→暂停发货→协商还款计划→必要时法律途径", "逾期催收"}
        ));
        QUESTION_BANK.put("基础档案", List.of(
                new String[]{"CHOICE", "商品编码的作用是？", "A.仅用于展示|B.唯一标识商品|C.统计人数|D.记录时间", "B", "编码规则"},
                new String[]{"CHOICE", "客户档案中信用额度的设置目的是？", "A.限制登录次数|B.控制赊购风险|C.计算库存|D.统计产量", "B", "信用管理"},
                new String[]{"CHOICE", "仓库档案中哪些信息是必填的？", "A.仓库名称和编码|B.装修风格|C.员工人数|D.周边环境", "A", "仓库管理"},
                new String[]{"CHOICE", "部门档案的层级关系表示什么？", "A.员工工龄|B.组织架构上下级|C.产品分类|D.仓库位置", "B", "部门管理"},
                new String[]{"TRUE_FALSE", "基础档案一旦创建就不能修改", "正确|错误", "错误", "档案维护"},
                new String[]{"TRUE_FALSE", "已被单据引用的档案不能直接删除", "正确|错误", "正确", "引用检查"},
                new String[]{"OPERATION", "请描述创建一个新商品档案的完整流程", "", "进入档案管理→新增商品→填写编码/名称/规格→设置分类→保存", "商品创建"},
                new String[]{"OPERATION", "请操作：批量导入客户档案", "", "准备导入模板→填写数据→上传文件→校验→确认导入", "批量导入"},
                new String[]{"OPERATION", "请演示如何维护供应商档案信息", "", "进入供应商管理→查找供应商→修改信息→保存→审核", "供应商维护"},
                new String[]{"SCENARIO", "系统中已存在2000+商品档案，部分编码不规范，如何制定整改方案？", "", "梳理编码规则→分批整改→导出对照表→批量修改→验证关联单据", "档案整改"}
        ));
    }

    public GeneratedExamVO generateExam(Long projectId, String module, String difficulty) {
        if (module == null || module.isBlank()) {
            throw new BizException("考核模块不能为空");
        }
        if (difficulty == null || !VALID_DIFFICULTIES.contains(difficulty)) {
            throw new BizException("难度级别无效，有效值: " + String.join(", ", VALID_DIFFICULTIES));
        }

        List<String[]> bank = QUESTION_BANK.get(module);
        if (bank == null) {
            bank = QUESTION_BANK.get("基础档案");
        }

        String difficultyPrefix = getDifficultyPrefix(difficulty);

        List<ExamQuestionVO> questions = new ArrayList<>();
        int qId = 1;

        int choiceIdx = 0, tfIdx = 0, opIdx = 0, scIdx = 0;
        for (String[] q : bank) {
            switch (q[0]) {
                case "CHOICE":
                    if (choiceIdx < 4) {
                        questions.add(buildQuestion(qId++, q, difficultyPrefix, 10));
                        choiceIdx++;
                    }
                    break;
                case "TRUE_FALSE":
                    if (tfIdx < 2) {
                        questions.add(buildQuestion(qId++, q, difficultyPrefix, 7));
                        tfIdx++;
                    }
                    break;
                case "OPERATION":
                    if (opIdx < 3) {
                        questions.add(buildQuestion(qId++, q, difficultyPrefix, 10));
                        opIdx++;
                    }
                    break;
                case "SCENARIO":
                    if (scIdx < 1) {
                        questions.add(buildQuestion(qId++, q, difficultyPrefix, 16));
                        scIdx++;
                    }
                    break;
            }
        }

        int totalScore = questions.stream().mapToInt(ExamQuestionVO::getScore).sum();
        normalizeScores(questions, totalScore);

        GeneratedExamVO vo = new GeneratedExamVO();
        vo.setModule(module);
        vo.setDifficulty(difficulty);
        vo.setTotalQuestions(questions.size());
        vo.setTotalScore(100);
        vo.setQuestions(questions);
        return vo;
    }

    private ExamQuestionVO buildQuestion(int id, String[] raw, String prefix, int score) {
        ExamQuestionVO q = new ExamQuestionVO();
        q.setQuestionId(id);
        q.setQuestionType(raw[0]);
        q.setQuestionText(prefix + raw[1]);
        if (raw[2] != null && !raw[2].isEmpty()) {
            q.setOptions(List.of(raw[2].split("\\|")));
        } else {
            q.setOptions(List.of());
        }
        q.setCorrectAnswer(raw[3]);
        q.setScore(score);
        q.setHint(raw[4]);
        return q;
    }

    private String getDifficultyPrefix(String difficulty) {
        return switch (difficulty) {
            case "进阶级" -> "【进阶】";
            case "综合级" -> "【综合】";
            default -> "";
        };
    }

    private void normalizeScores(List<ExamQuestionVO> questions, int currentTotal) {
        if (questions.isEmpty() || currentTotal == 100) return;
        int diff = 100 - currentTotal;
        questions.get(questions.size() - 1).setScore(
                questions.get(questions.size() - 1).getScore() + diff);
    }
}
