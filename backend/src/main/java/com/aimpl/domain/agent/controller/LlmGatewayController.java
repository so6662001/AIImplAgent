package com.aimpl.domain.agent.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.agent.dto.LlmRequestDTO;
import com.aimpl.domain.agent.service.LlmGatewayService;
import com.aimpl.domain.agent.vo.LlmResponseVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/llm-gateway")
@RequiredArgsConstructor
public class LlmGatewayController {

    private final LlmGatewayService llmGatewayService;

    @PostMapping("/complete")
    public R<LlmResponseVO> complete(@Valid @RequestBody LlmRequestDTO request) {
        return R.ok(llmGatewayService.complete(request));
    }

    @GetMapping("/test/{agentCode}")
    public R<LlmResponseVO> test(@PathVariable String agentCode) {
        return R.ok(llmGatewayService.testAgent(agentCode));
    }
}
