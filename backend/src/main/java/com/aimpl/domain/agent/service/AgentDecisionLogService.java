package com.aimpl.domain.agent.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.agent.entity.AgentDecisionLog;
import com.aimpl.domain.agent.mapper.AgentDecisionLogMapper;
import com.aimpl.domain.agent.vo.AgentPerformanceVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentDecisionLogService extends ServiceImpl<AgentDecisionLogMapper, AgentDecisionLog> {

    @Transactional
    public AgentDecisionLog logDecision(String agentCode, Long projectId, String triggerType,
                                        String inputSummary, String outputSummary,
                                        String modelUsed, int executionTimeMs) {
        AgentDecisionLog log = new AgentDecisionLog();
        log.setAgentCode(agentCode);
        log.setProjectId(projectId);
        log.setTriggerType(triggerType);
        log.setInputSummary(inputSummary);
        log.setOutputSummary(outputSummary);
        log.setModelUsed(modelUsed);
        log.setExecutionTimeMs(executionTimeMs);
        log.setHumanReviewed(false);
        save(log);
        return log;
    }

    @Transactional
    public AgentDecisionLog reviewDecision(Long id, String reviewResult, String reviewComment) {
        AgentDecisionLog log = getById(id);
        if (log == null) {
            throw new BizException("决策日志不存在: " + id);
        }
        log.setHumanReviewed(true);
        log.setReviewResult(reviewResult);
        log.setReviewComment(reviewComment);
        updateById(log);
        return log;
    }

    public List<AgentDecisionLog> listByAgent(String agentCode, Long projectId) {
        LambdaQueryWrapper<AgentDecisionLog> wrapper = new LambdaQueryWrapper<>();
        if (agentCode != null && !agentCode.isBlank()) {
            wrapper.eq(AgentDecisionLog::getAgentCode, agentCode);
        }
        if (projectId != null) {
            wrapper.eq(AgentDecisionLog::getProjectId, projectId);
        }
        wrapper.orderByDesc(AgentDecisionLog::getCreateTime);
        return list(wrapper);
    }

    public AgentPerformanceVO getPerformance(String agentCode) {
        List<AgentDecisionLog> logs = list(new LambdaQueryWrapper<AgentDecisionLog>()
                .eq(AgentDecisionLog::getAgentCode, agentCode));

        AgentPerformanceVO vo = new AgentPerformanceVO();
        vo.setAgentCode(agentCode);
        vo.setTotalDecisions(logs.size());

        vo.setAvgExecutionTimeMs(logs.stream()
                .filter(l -> l.getExecutionTimeMs() != null)
                .mapToInt(AgentDecisionLog::getExecutionTimeMs)
                .average()
                .orElse(0.0));

        vo.setReviewedCount(logs.stream()
                .filter(l -> Boolean.TRUE.equals(l.getHumanReviewed()))
                .count());

        vo.setApprovedCount(logs.stream()
                .filter(l -> "APPROVED".equals(l.getReviewResult()))
                .count());

        vo.setModifiedCount(logs.stream()
                .filter(l -> "MODIFIED".equals(l.getReviewResult()))
                .count());

        vo.setRejectedCount(logs.stream()
                .filter(l -> "REJECTED".equals(l.getReviewResult()))
                .count());

        if (vo.getReviewedCount() > 0) {
            vo.setApprovalRate(BigDecimal.valueOf(vo.getApprovedCount())
                    .divide(BigDecimal.valueOf(vo.getReviewedCount()), 4, RoundingMode.HALF_UP));
        } else {
            vo.setApprovalRate(BigDecimal.ZERO);
        }

        return vo;
    }

    public List<AgentPerformanceVO> getAllPerformance() {
        List<AgentDecisionLog> all = list();
        return all.stream()
                .map(AgentDecisionLog::getAgentCode)
                .distinct()
                .map(this::getPerformance)
                .collect(Collectors.toList());
    }
}
