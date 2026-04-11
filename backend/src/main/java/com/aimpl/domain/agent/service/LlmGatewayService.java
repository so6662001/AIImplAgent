package com.aimpl.domain.agent.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.agent.dto.LlmRequestDTO;
import com.aimpl.domain.agent.entity.AgentConfig;
import com.aimpl.domain.agent.entity.AgentDecisionLog;
import com.aimpl.domain.agent.entity.LlmProviderConfig;
import com.aimpl.domain.agent.vo.LlmResponseVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmGatewayService {

    private final LlmProviderConfigService providerService;
    private final AgentConfigService agentConfigService;
    private final AgentDecisionLogService decisionLogService;

    /**
     * Send a prompt to the LLM assigned to a specific agent.
     * Routes to the correct provider based on AgentConfig.
     * Logs the decision in AgentDecisionLog.
     * Falls back to fallback provider if primary fails.
     */
    public LlmResponseVO complete(LlmRequestDTO request) {
        long startTime = System.currentTimeMillis();

        AgentConfig agentConfig = agentConfigService.getOne(
                new LambdaQueryWrapper<AgentConfig>()
                        .eq(AgentConfig::getAgentCode, request.getAgentCode()));
        if (agentConfig == null) {
            throw new BizException("智能体配置不存在: " + request.getAgentCode());
        }
        if (!Boolean.TRUE.equals(agentConfig.getEnabled())) {
            throw new BizException("智能体已禁用: " + request.getAgentCode());
        }

        LlmProviderConfig primaryProvider = providerService.getById(agentConfig.getLlmProviderId());
        if (primaryProvider == null) {
            throw new BizException("LLM供应商配置不存在, id: " + agentConfig.getLlmProviderId());
        }

        boolean fallbackUsed = false;
        LlmProviderConfig usedProvider = primaryProvider;
        String response;

        try {
            response = callLlm(primaryProvider, request);
        } catch (Exception e) {
            log.warn("主LLM调用失败(provider={}): {}", primaryProvider.getProviderName(), e.getMessage());
            if (agentConfig.getFallbackLlmProviderId() != null) {
                LlmProviderConfig fallbackProvider = providerService.getById(agentConfig.getFallbackLlmProviderId());
                if (fallbackProvider == null) {
                    throw new BizException("备用LLM供应商配置不存在, id: " + agentConfig.getFallbackLlmProviderId());
                }
                response = callLlm(fallbackProvider, request);
                usedProvider = fallbackProvider;
                fallbackUsed = true;
            } else {
                throw new BizException("LLM调用失败且无备用供应商: " + e.getMessage());
            }
        }

        long executionTimeMs = System.currentTimeMillis() - startTime;
        int estimatedTokens = estimateTokens(request.getPrompt(), response);

        String inputSummary = request.getPrompt().length() > 200
                ? request.getPrompt().substring(0, 200) + "..."
                : request.getPrompt();
        String outputSummary = response.length() > 200
                ? response.substring(0, 200) + "..."
                : response;

        AgentDecisionLog decisionLog = decisionLogService.logDecision(
                request.getAgentCode(),
                request.getProjectId(),
                "LLM_CALL",
                inputSummary,
                outputSummary,
                usedProvider.getModelName(),
                (int) executionTimeMs
        );

        LlmResponseVO vo = new LlmResponseVO();
        vo.setAgentCode(request.getAgentCode());
        vo.setProviderName(usedProvider.getProviderName());
        vo.setModelName(usedProvider.getModelName());
        vo.setResponse(response);
        vo.setTokensUsed(estimatedTokens);
        vo.setExecutionTimeMs(executionTimeMs);
        vo.setFallbackUsed(fallbackUsed);
        vo.setDecisionLogId(decisionLog.getId());
        return vo;
    }

    /**
     * Quick test with a default prompt to verify config.
     */
    public LlmResponseVO testAgent(String agentCode) {
        LlmRequestDTO request = new LlmRequestDTO();
        request.setAgentCode(agentCode);
        request.setPrompt("这是一个测试请求，请确认智能体" + agentCode + "已正确配置并可用。");
        return complete(request);
    }

    /**
     * Call LLM provider. Currently uses mock response generator.
     * Structure is ready for real HTTP call swap-in.
     */
    private String callLlm(LlmProviderConfig provider, LlmRequestDTO request) {
        log.info("LLM调用 [provider={}, model={}, endpoint={}] agentCode={}, promptLength={}",
                provider.getProviderName(),
                provider.getModelName(),
                provider.getApiEndpoint(),
                request.getAgentCode(),
                request.getPrompt().length());

        // TODO: Replace with real HTTP call when ready
        // HttpRequest would be:
        //   POST {provider.getApiEndpoint()}/chat/completions
        //   Headers: Authorization: Bearer {provider.getApiKey()}
        //   Body: { model: provider.getModelName(), messages: [{role:"user", content: prompt}],
        //           temperature: request.getTemperature() or provider.getTemperature(),
        //           max_tokens: request.getMaxTokens() or provider.getMaxTokens() }

        return generateMockResponse(request.getAgentCode(), request.getPrompt());
    }

    /**
     * Generate a mock response (placeholder until real LLM integration).
     * Returns contextually relevant responses based on the agent and prompt.
     */
    private String generateMockResponse(String agentCode, String prompt) {
        String base = switch (agentCode.toUpperCase()) {
            case "DISPATCH" ->
                    "基于项目特征分析，推荐以下PM候选人：\n" +
                    "1. 候选人A - 匹配度95%，擅长钢贸行业，当前空闲\n" +
                    "2. 候选人B - 匹配度88%，有类似规模项目经验\n" +
                    "3. 候选人C - 匹配度82%，区域优势明显\n" +
                    "建议优先安排候选人A，预计可在3个工作日内到岗。";
            case "RESEARCH" ->
                    "根据客户调研信息，分析如下：\n" +
                    "1. 企业概况：该企业属于钢铁行业中型企业\n" +
                    "2. 业务模式：以贸易为主，兼有加工业务\n" +
                    "3. 核心需求：进销存管理、库存优化、财务对账\n" +
                    "4. 风险评估：整体风险中等，主要关注数据迁移和培训覆盖\n" +
                    "建议重点关注库存周转率优化和客户信用管理。";
            case "PLAN" ->
                    "基于项目范围和约束条件，建议交付计划如下：\n" +
                    "1. 调研阶段：5个工作日\n" +
                    "2. 方案确认：3个工作日\n" +
                    "3. 模拟演练：10个工作日\n" +
                    "4. 培训实施：15个工作日\n" +
                    "5. 数据导入：8个工作日\n" +
                    "6. 上线跟进：10个工作日\n" +
                    "预计总工期60个工作日，建议配置PM1名+实施顾问2名。";
            case "TRAINING" -> generateTrainingMockResponse(prompt);
            case "DATA_GOVERNANCE" ->
                    "数据治理建议：\n" +
                    "1. 数据质量检查：发现3项数据异常，建议核实修正\n" +
                    "2. 主数据标准化：客户编码、产品编码需统一规范\n" +
                    "3. 历史数据清洗：建议清理无效记录，预计影响200条数据\n" +
                    "4. 数据迁移策略：推荐分批次导入，优先基础档案后导入余额\n" +
                    "总体数据质量评分：78/100，达到可导入标准。";
            case "SIMULATION" ->
                    "模拟演练评估结果：\n" +
                    "1. 采购入库场景：通过，操作规范\n" +
                    "2. 销售出库场景：通过，存在轻微偏差\n" +
                    "3. 库存盘点场景：通过，建议优化操作流程\n" +
                    "4. 财务结算场景：待执行\n" +
                    "整体通过率：75%，建议对未执行场景尽快安排演练。";
            case "SUPPORT" ->
                    "问题诊断结果：\n" +
                    "1. 问题类型：操作类问题(L1)\n" +
                    "2. 根因分析：用户对系统操作流程不熟悉\n" +
                    "3. 解决方案：参考操作手册第3章，按步骤执行即可\n" +
                    "4. 预防建议：安排针对性复训\n" +
                    "预计处理时间：15分钟。";
            case "REPORT" ->
                    "交付报告生成建议：\n" +
                    "1. 项目概况章节：已自动采集项目基本信息\n" +
                    "2. 交付成果章节：培训通过率92%，数据导入完成率100%\n" +
                    "3. 过程质量章节：工单解决率88%，进度偏差-3%\n" +
                    "4. 遗留问题章节：2个待解决工单\n" +
                    "综合评分：85分，建议进入客户签字验收流程。";
            default ->
                    "收到请求，智能体[" + agentCode + "]已处理。\n" +
                    "基于输入分析结果如下：\n" +
                    "1. 请求已接收并解析\n" +
                    "2. 相关数据已检索\n" +
                    "3. 分析完成，结果已生成\n" +
                    "如需进一步操作，请提供更多上下文信息。";
        };

        if (prompt.contains("紧急") || prompt.contains("urgent")) {
            base = "【紧急处理】" + base;
        }

        return base;
    }

    private String generateTrainingMockResponse(String prompt) {
        if (prompt.contains("考核") || prompt.contains("考试")) {
            return "培训考核分析：\n" +
                   "1. 当前整体通过率：85%\n" +
                   "2. 薄弱模块：库存管理（通过率仅70%）\n" +
                   "3. KA用户达标情况：90%已通过\n" +
                   "4. 建议：对库存管理模块安排补学，重点加强实操练习\n" +
                   "上线准备度评估：基本具备，建议完成补学后再做最终确认。";
        }
        if (prompt.contains("课程") || prompt.contains("教材")) {
            return "培训课程建议：\n" +
                   "1. 根据行业类型和岗位特点，推荐以下必学课程\n" +
                   "2. 建议培训时长：每模块2-4课时\n" +
                   "3. 教材准备：系统操作手册+业务流程图+考核试题\n" +
                   "4. 培训方式：理论+实操结合，建议比例3:7";
        }
        return "培训智能体分析结果：\n" +
               "1. 基于学员画像和进度数据，当前培训进展正常\n" +
               "2. 建议关注低出勤率学员，及时跟进\n" +
               "3. 文档提交率需提升，建议加强日常检查\n" +
               "4. 整体培训质量良好，按计划推进即可。";
    }

    private int estimateTokens(String prompt, String response) {
        return (prompt.length() + response.length()) / 2;
    }
}
