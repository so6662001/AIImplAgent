package com.aimpl.domain.agent.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.agent.entity.AgentDecisionLog;
import com.aimpl.domain.agent.service.AgentDecisionLogService;
import com.aimpl.domain.agent.vo.AgentPerformanceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent-decisions")
@RequiredArgsConstructor
public class AgentDecisionController {

    private final AgentDecisionLogService agentDecisionLogService;

    @PostMapping
    public R<AgentDecisionLog> logDecision(@RequestBody Map<String, Object> body) {
        String agentCode = (String) body.get("agentCode");
        Long projectId = body.get("projectId") != null
                ? Long.valueOf(body.get("projectId").toString()) : null;
        String triggerType = (String) body.get("triggerType");
        String inputSummary = (String) body.get("inputSummary");
        String outputSummary = (String) body.get("outputSummary");
        String modelUsed = (String) body.get("modelUsed");
        int executionTimeMs = body.get("executionTimeMs") != null
                ? Integer.parseInt(body.get("executionTimeMs").toString()) : 0;

        return R.ok(agentDecisionLogService.logDecision(
                agentCode, projectId, triggerType,
                inputSummary, outputSummary, modelUsed, executionTimeMs));
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
