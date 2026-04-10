package com.aimpl.domain.agent.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.agent.dto.AgentConfigCreateDTO;
import com.aimpl.domain.agent.entity.AgentConfig;
import com.aimpl.domain.agent.service.AgentConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent-configs")
@RequiredArgsConstructor
public class AgentConfigController {

    private final AgentConfigService agentConfigService;

    @PostMapping
    public R<AgentConfig> create(@Valid @RequestBody AgentConfigCreateDTO dto) {
        return R.ok(agentConfigService.createAgent(dto));
    }

    @GetMapping
    public R<List<AgentConfig>> list() {
        return R.ok(agentConfigService.list());
    }

    @GetMapping("/{id}")
    public R<AgentConfig> get(@PathVariable Long id) {
        return R.ok(agentConfigService.getById(id));
    }

    @PutMapping("/{id}")
    public R<AgentConfig> update(@PathVariable Long id, @Valid @RequestBody AgentConfigCreateDTO dto) {
        return R.ok(agentConfigService.updateAgent(id, dto));
    }
}
