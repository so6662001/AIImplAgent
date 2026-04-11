package com.aimpl.domain.agent.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.agent.dto.AgentDecisionLogDTO;
import com.aimpl.domain.agent.entity.AgentDecisionLog;
import com.aimpl.domain.agent.service.AgentDecisionLogService;
import com.aimpl.domain.agent.vo.AgentPerformanceVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent-decisions")
@RequiredArgsConstructor
public class AgentDecisionController {

    private final AgentDecisionLogService agentDecisionLogService;

    @PostMapping
    public R<AgentDecisionLog> logDecision(@Valid @RequestBody AgentDecisionLogDTO dto) {
        return R.ok(agentDecisionLogService.logDecision(
                dto.getAgentCode(), dto.getProjectId(), dto.getTriggerType(),
                dto.getInputSummary(), dto.getOutputSummary(), dto.getModelUsed(),
                dto.getExecutionTimeMs() != null ? dto.getExecutionTimeMs() : 0));
    }

    @GetMapping
    public R<List<AgentDecisionLog>> list(
            @RequestParam(required = false) String agentCode,
            @RequestParam(required = false) Long projectId) {
        return R.ok(agentDecisionLogService.listByAgent(agentCode, projectId));
    }

    @PutMapping("/{id}/review")
    public R<AgentDecisionLog> review(
            @PathVariable Long id,
            @RequestParam String reviewResult,
            @RequestParam(required = false) String reviewComment) {
        return R.ok(agentDecisionLogService.reviewDecision(id, reviewResult, reviewComment));
    }

    @GetMapping("/performance/{agentCode}")
    public R<AgentPerformanceVO> performance(@PathVariable String agentCode) {
        return R.ok(agentDecisionLogService.getPerformance(agentCode));
    }

    @GetMapping("/performance")
    public R<List<AgentPerformanceVO>> allPerformance() {
        return R.ok(agentDecisionLogService.getAllPerformance());
    }
}
