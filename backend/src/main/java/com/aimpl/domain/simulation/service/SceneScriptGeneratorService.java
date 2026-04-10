package com.aimpl.domain.simulation.service;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.common.enums.SceneType;
import com.aimpl.common.enums.SimulationStatus;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.simulation.entity.SimulationScene;
import com.aimpl.domain.simulation.mapper.SimulationSceneMapper;
import com.aimpl.domain.simulation.vo.GeneratedSceneVO;
import com.aimpl.domain.simulation.vo.SceneStepVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SceneScriptGeneratorService {

    private final ProjectMapper projectMapper;
    private final SimulationSceneMapper simulationSceneMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<GeneratedSceneVO> generateScenes(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        IndustryType industryType = project.getIndustryType() != null
                ? project.getIndustryType() : IndustryType.STEEL_TRADER;
        String modules = project.getModules() != null ? project.getModules() : "";

        List<GeneratedSceneVO> results = new ArrayList<>();

        results.add(buildPurchaseInbound(industryType));
        results.add(buildSalesOutbound(industryType));
        results.add(buildInventoryCheck(industryType));
        results.add(buildTransfer(industryType));
        results.add(buildFinancialSettlement(industryType));
        results.add(buildFullProcess(industryType));

        if (industryType == IndustryType.STEEL_MILL && modules.toUpperCase().contains("MES")) {
            results.add(buildMesProductionScene());
        }

        for (GeneratedSceneVO vo : results) {
            long nameExists = simulationSceneMapper.selectCount(
                    new LambdaQueryWrapper<SimulationScene>()
                            .eq(SimulationScene::getProjectId, projectId)
                            .eq(SimulationScene::getSceneName, vo.getSceneName()));
            if (nameExists > 0) {
                continue;
            }

            SimulationScene entity = new SimulationScene();
            entity.setProjectId(projectId);
            entity.setSceneName(vo.getSceneName());
            entity.setSceneType(SceneType.valueOf(vo.getSceneType()));
            entity.setDescription(vo.getDescription());
            entity.setSteps(serializeSteps(vo.getSteps()));
            entity.setExpectedResult(vo.getExpectedResult());
            entity.setStatus(SimulationStatus.PENDING);
            simulationSceneMapper.insert(entity);
            vo.setId(entity.getId());
        }

        return results;
    }

    private String serializeSteps(List<SceneStepVO> steps) {
        try {
            return objectMapper.writeValueAsString(steps);
        } catch (JsonProcessingException e) {
            StringBuilder sb = new StringBuilder();
            for (SceneStepVO step : steps) {
                sb.append("步骤").append(step.getStepNumber()).append(": ")
                        .append(step.getInstruction()).append("\n");
            }
            return sb.toString();
        }
    }

    private GeneratedSceneVO buildPurchaseInbound(IndustryType industryType) {
        GeneratedSceneVO vo = new GeneratedSceneVO();
        vo.setSceneName("采购入库全流程");
        vo.setSceneType(SceneType.PURCHASE_INBOUND.name());
        vo.setDescription("模拟从创建采购订单到货物入库的完整业务流程，验证采购管理和库存管理的联动");
        vo.setDifficulty("基础");

        String product = industryType == IndustryType.STEEL_MILL ? "废钢" : "热轧卷板";
        String spec = industryType == IndustryType.STEEL_MILL ? "重型废钢" : "3.0*1250*C";
        String supplier = "宝山钢铁股份有限公司";

        List<SceneStepVO> steps = new ArrayList<>();
        steps.add(new SceneStepVO(1, "登录系统，进入采购管理模块",
                "成功进入采购管理主界面", "页面正确加载，显示采购管理功能菜单"));
        steps.add(new SceneStepVO(2, "新建采购单，选择供应商\"" + supplier + "\"",
                "采购单创建成功，供应商信息自动填充", "供应商名称、联系方式、结算方式正确显示"));
        steps.add(new SceneStepVO(3, "添加采购明细：品名=" + product + ", 规格=" + spec + ", 数量=100, 单价=4200",
                "明细行添加成功，金额自动计算", "金额=420000.00元，含税金额正确"));
        steps.add(new SceneStepVO(4, "提交采购单审批",
                "采购单状态变更为待审批", "审批流程启动，通知相关审批人"));
        steps.add(new SceneStepVO(5, "审批通过采购单",
                "采购单状态变更为已审批", "采购单可用于后续入库操作"));
        steps.add(new SceneStepVO(6, "进入入库管理，创建入库单并关联采购单",
                "入库单创建成功，自动带入采购明细", "品名、规格、数量与采购单一致"));
        steps.add(new SceneStepVO(7, "录入实际入库数量和重量：数量=98, 重量=245.00吨",
                "实际数量录入成功", "系统标记采购差异：数量差异-2"));
        steps.add(new SceneStepVO(8, "确认入库，检查库存变动",
                "库存增加，入库单状态为已完成", "库存数量增加98件，采购应付增加420000元"));

        vo.setSteps(steps);
        vo.setExpectedResult("库存增加98件/245吨，采购应付增加420000元，采购单与入库单关联正确");
        return vo;
    }

    private GeneratedSceneVO buildSalesOutbound(IndustryType industryType) {
        GeneratedSceneVO vo = new GeneratedSceneVO();
        vo.setSceneName("销售出库全流程");
        vo.setSceneType(SceneType.SALES_OUTBOUND.name());
        vo.setDescription("模拟从创建销售订单到货物出库的完整业务流程，验证销售管理和库存扣减的联动");
        vo.setDifficulty("基础");

        String product = industryType == IndustryType.PROCESSING_CENTER ? "开平板" : "热轧卷板";
        String spec = industryType == IndustryType.PROCESSING_CENTER ? "4.0*1250*2500" : "3.0*1250*C";
        String customer = "上海建工集团有限公司";

        List<SceneStepVO> steps = new ArrayList<>();
        steps.add(new SceneStepVO(1, "登录系统，进入销售管理模块",
                "成功进入销售管理主界面", "页面正确加载，显示销售管理功能菜单"));
        steps.add(new SceneStepVO(2, "新建销售单，选择客户\"" + customer + "\"",
                "销售单创建成功，客户信息自动填充", "客户名称、联系方式、结算方式正确显示"));
        steps.add(new SceneStepVO(3, "添加销售明细：品名=" + product + ", 规格=" + spec + ", 数量=50, 单价=4500",
                "明细行添加成功，金额自动计算", "金额=225000.00元，含税金额正确"));
        steps.add(new SceneStepVO(4, "提交销售单审批",
                "销售单状态变更为待审批", "审批流程启动"));
        steps.add(new SceneStepVO(5, "审批通过销售单",
                "销售单状态变更为已审批", "销售单可用于后续出库操作"));
        steps.add(new SceneStepVO(6, "进入出库管理，创建出库单并关联销售单",
                "出库单创建成功，自动带入销售明细", "品名、规格、数量与销售单一致"));
        steps.add(new SceneStepVO(7, "选择出库仓库和库位，确认出库数量：数量=50, 重量=125.00吨",
                "出库数量确认成功", "库存充足，可正常出库"));
        steps.add(new SceneStepVO(8, "确认出库，检查库存变动和应收账款",
                "库存减少，出库单状态为已完成", "库存数量减少50件，销售应收增加225000元"));

        vo.setSteps(steps);
        vo.setExpectedResult("库存减少50件/125吨，销售应收增加225000元，销售单与出库单关联正确");
        return vo;
    }

    private GeneratedSceneVO buildInventoryCheck(IndustryType industryType) {
        GeneratedSceneVO vo = new GeneratedSceneVO();
        vo.setSceneName("库存盘点流程");
        vo.setSceneType(SceneType.INVENTORY_CHECK.name());
        vo.setDescription("模拟仓库盘点操作，验证系统库存与实际库存的对比和调整功能");
        vo.setDifficulty("进阶");

        List<SceneStepVO> steps = new ArrayList<>();
        steps.add(new SceneStepVO(1, "登录系统，进入库存管理-盘点模块",
                "成功进入盘点功能界面", "页面正确加载，显示盘点管理菜单"));
        steps.add(new SceneStepVO(2, "创建盘点任务，选择盘点仓库：主仓库",
                "盘点任务创建成功", "系统生成盘点单号，状态为待盘点"));
        steps.add(new SceneStepVO(3, "系统自动生成盘点清单，包含该仓库所有库存商品",
                "盘点清单生成成功", "清单包含所有品名、规格、系统数量"));
        steps.add(new SceneStepVO(4, "录入实际盘点数量（模拟部分商品存在盈亏）：热轧卷板实盘98（系统100），螺纹钢实盘52（系统50）",
                "盘点数量录入成功", "系统自动计算盈亏差异"));
        steps.add(new SceneStepVO(5, "提交盘点结果审核",
                "盘点单状态变更为待审核", "盈亏明细正确显示：热轧卷板盘亏2件，螺纹钢盘盈2件"));
        steps.add(new SceneStepVO(6, "审核通过盘点结果，确认库存调整",
                "库存数量按盘点结果调整", "库存变动记录生成，盘点单状态为已完成"));
        steps.add(new SceneStepVO(7, "查看盘盈盘亏报表",
                "报表数据正确展示", "盘盈盘亏金额计算正确"));

        vo.setSteps(steps);
        vo.setExpectedResult("库存按实盘数量调整，盘点差异记录完整，盈亏金额计算正确");
        return vo;
    }

    private GeneratedSceneVO buildTransfer(IndustryType industryType) {
        GeneratedSceneVO vo = new GeneratedSceneVO();
        vo.setSceneName("调拨流程");
        vo.setSceneType(SceneType.TRANSFER.name());
        vo.setDescription("模拟仓库间调拨操作，验证库存在不同仓库间的转移");
        vo.setDifficulty("基础");

        List<SceneStepVO> steps = new ArrayList<>();
        steps.add(new SceneStepVO(1, "登录系统，进入库存管理-调拨模块",
                "成功进入调拨功能界面", "页面正确加载，显示调拨管理菜单"));
        steps.add(new SceneStepVO(2, "创建调拨单，选择调出仓库：主仓库，调入仓库：二号仓库",
                "调拨单创建成功", "调出仓库和调入仓库信息正确"));
        steps.add(new SceneStepVO(3, "添加调拨明细：品名=热轧卷板, 规格=3.0*1250*C, 数量=20, 重量=50吨",
                "明细添加成功", "调出仓库库存充足，可正常调拨"));
        steps.add(new SceneStepVO(4, "提交调拨单审批",
                "调拨单状态变更为待审批", "审批流程启动"));
        steps.add(new SceneStepVO(5, "审批通过，确认调出",
                "主仓库库存减少20件", "调拨单状态变更为在途"));
        steps.add(new SceneStepVO(6, "调入仓库确认收货",
                "二号仓库库存增加20件", "调拨单状态变更为已完成"));
        steps.add(new SceneStepVO(7, "查看调拨记录和库存变动",
                "调拨记录完整", "两个仓库库存数据一致，调拨前后总库存不变"));

        vo.setSteps(steps);
        vo.setExpectedResult("主仓库减少20件/50吨，二号仓库增加20件/50吨，总库存不变，调拨记录完整");
        return vo;
    }

    private GeneratedSceneVO buildFinancialSettlement(IndustryType industryType) {
        GeneratedSceneVO vo = new GeneratedSceneVO();
        vo.setSceneName("财务结算流程");
        vo.setSceneType(SceneType.FINANCIAL_SETTLEMENT.name());
        vo.setDescription("模拟采购应付和销售应收的财务结算流程，包括对账、开票、收付款");
        vo.setDifficulty("进阶");

        List<SceneStepVO> steps = new ArrayList<>();
        steps.add(new SceneStepVO(1, "登录系统，进入财务管理模块",
                "成功进入财务管理主界面", "页面正确加载，显示财务管理功能菜单"));
        steps.add(new SceneStepVO(2, "进入应付管理，查看供应商\"宝山钢铁股份有限公司\"的应付账款",
                "应付明细正确展示", "应付金额与采购入库单金额一致"));
        steps.add(new SceneStepVO(3, "创建付款申请单，选择待付账单",
                "付款申请单创建成功", "付款金额=420000元"));
        steps.add(new SceneStepVO(4, "审批通过付款申请",
                "付款申请状态变更为已审批", "可进行实际付款操作"));
        steps.add(new SceneStepVO(5, "执行付款操作，选择银行账户，确认付款",
                "付款完成，应付余额减少", "银行存款减少420000元，应付账款减少420000元"));
        steps.add(new SceneStepVO(6, "进入应收管理，查看客户\"上海建工集团有限公司\"的应收账款",
                "应收明细正确展示", "应收金额与销售出库单金额一致"));
        steps.add(new SceneStepVO(7, "录入客户回款，确认收款金额=225000元",
                "收款完成，应收余额减少", "银行存款增加225000元，应收账款减少225000元"));
        steps.add(new SceneStepVO(8, "查看资金流水和往来余额表",
                "报表数据正确", "收付款记录完整，余额计算正确"));

        vo.setSteps(steps);
        vo.setExpectedResult("应付减少420000元，应收减少225000元，银行流水记录完整，往来余额正确");
        return vo;
    }

    private GeneratedSceneVO buildFullProcess(IndustryType industryType) {
        GeneratedSceneVO vo = new GeneratedSceneVO();
        vo.setSceneName("全流程串联演练");
        vo.setSceneType(SceneType.FULL_PROCESS.name());
        vo.setDescription("串联采购→入库→销售→出库→结算的完整业务闭环，验证各环节数据联动");
        vo.setDifficulty("综合");

        List<SceneStepVO> steps = new ArrayList<>();
        steps.add(new SceneStepVO(1, "创建采购单：供应商=沙钢, 品名=螺纹钢, 规格=Φ20*12000, 数量=200, 单价=3600",
                "采购单创建成功，金额=720000元", "采购单号生成，状态为待审批"));
        steps.add(new SceneStepVO(2, "审批通过采购单",
                "采购单审批通过", "可进行入库操作"));
        steps.add(new SceneStepVO(3, "创建入库单关联采购单，录入实际入库：数量=200, 重量=500吨",
                "入库完成，库存增加", "库存增加200件/500吨螺纹钢"));
        steps.add(new SceneStepVO(4, "创建销售单：客户=中建三局, 品名=螺纹钢, 规格=Φ20*12000, 数量=80, 单价=3900",
                "销售单创建成功，金额=312000元", "销售单号生成，状态为待审批"));
        steps.add(new SceneStepVO(5, "审批通过销售单",
                "销售单审批通过", "可进行出库操作"));
        steps.add(new SceneStepVO(6, "创建出库单关联销售单，确认出库：数量=80, 重量=200吨",
                "出库完成，库存减少", "库存减少80件/200吨，剩余120件/300吨"));
        steps.add(new SceneStepVO(7, "执行采购付款：金额=720000元",
                "付款完成", "应付减少720000元，银行减少720000元"));
        steps.add(new SceneStepVO(8, "录入销售回款：金额=312000元",
                "收款完成", "应收减少312000元，银行增加312000元"));
        steps.add(new SceneStepVO(9, "查看利润分析：销售收入312000 - 采购成本(按比例)288000 = 毛利24000",
                "利润报表正确", "毛利率约7.7%，数据逻辑一致"));
        steps.add(new SceneStepVO(10, "查看库存现存量报表",
                "库存报表正确", "螺纹钢剩余120件/300吨，成本金额正确"));

        vo.setSteps(steps);
        vo.setExpectedResult("采购入库200件→销售出库80件→剩余120件，应付720000→已付清，应收312000→已收回，毛利24000元");
        return vo;
    }

    private GeneratedSceneVO buildMesProductionScene() {
        GeneratedSceneVO vo = new GeneratedSceneVO();
        vo.setSceneName("MES生产管理流程");
        vo.setSceneType(SceneType.FULL_PROCESS.name());
        vo.setDescription("模拟MES生产工单从创建到完工入库的全流程，验证生产管理与库存联动");
        vo.setDifficulty("进阶");

        List<SceneStepVO> steps = new ArrayList<>();
        steps.add(new SceneStepVO(1, "登录系统，进入MES生产管理模块",
                "成功进入MES主界面", "页面正确加载，显示生产管理功能菜单"));
        steps.add(new SceneStepVO(2, "创建生产工单：成品=热轧卷板, 规格=3.0*1250*C, 计划数量=500吨",
                "生产工单创建成功", "工单号生成，状态为计划中"));
        steps.add(new SceneStepVO(3, "配置BOM物料清单：铁矿石1600kg/吨, 焦炭350kg/吨, 废钢200kg/吨",
                "BOM配置成功", "原料需求自动计算"));
        steps.add(new SceneStepVO(4, "下达生产工单，原料自动扣减",
                "工单下达成功，原料库存扣减", "铁矿石扣减800吨，焦炭扣减175吨，废钢扣减100吨"));
        steps.add(new SceneStepVO(5, "录入生产过程数据：实际产量=490吨，合格率=98%",
                "生产数据录入成功", "成品率和合格率统计正确"));
        steps.add(new SceneStepVO(6, "完工入库：热轧卷板490吨入库",
                "完工入库成功", "成品库存增加490吨"));
        steps.add(new SceneStepVO(7, "查看生产成本分析",
                "成本报表正确", "单位成本计算正确，原料消耗与产出匹配"));

        vo.setSteps(steps);
        vo.setExpectedResult("原料消耗正确，成品入库490吨，生产成本计算准确，库存数据一致");
        return vo;
    }
}
